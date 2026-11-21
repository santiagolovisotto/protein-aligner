package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.AlignProteinsService;
import com.santiagolovisotto.proteinaligner.alignment.Aligner;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentRequest;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentResult;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentType;
import com.santiagolovisotto.proteinaligner.alignment.SequenceRange;
import com.santiagolovisotto.proteinaligner.pdb.PdbCode;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntryNotFoundException;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntrySource;
import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;
import com.santiagolovisotto.proteinaligner.pdb.PdbUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlignControllerTest {

    private static final PdbCode HEMOGLOBINA = new PdbCode("4HHB");

    private final AtomicReference<AlignmentRequest> recibida = new AtomicReference<>();

    private final Aligner alineadorFalso = request -> {
        if (request.type() == AlignmentType.LOCAL) {
            throw new UnsupportedOperationException("El alineamiento local todavia no esta implementado");
        }
        recibida.set(request);
        return new AlignmentResult(AlignmentType.GLOBAL, request.parameters(), 7.0,
                "ACDE", "AC-E", "|| |", 3, 3, new SequenceRange(1, 4), new SequenceRange(1, 3));
    };

    private static PdbEntity entidad(int id, String secuencia) {
        return new PdbEntity(HEMOGLOBINA, id, List.of("A"), "Entidad " + id, "Homo sapiens (9606)", secuencia);
    }

    private static final PdbEntrySource FUENTE = code -> {
        if (!code.equals(HEMOGLOBINA)) {
            throw new PdbEntryNotFoundException(code);
        }
        return List.of(entidad(1, "VLSPADK"), entidad(2, "VHLTPEEK"), entidad(3, "ATGCATGC"));
    };

    private MockMvc mvc(PdbEntrySource fuente, int maximo) {
        AlignProteinsService servicio = new AlignProteinsService(new PdbLookupService(fuente), alineadorFalso, maximo);
        return MockMvcBuilders.standaloneSetup(new AlignController(servicio))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private ResultActions alinear(MockMvc mvc, String json) throws Exception {
        return mvc.perform(post("/api/align").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String cuerpo(String extra) {
        return """
                {"query":{"code":"4HHB","entityId":1},"target":{"code":"4HHB","entityId":2},"type":"GLOBAL"%s}"""
                .formatted(extra);
    }

    @Test
    void alineaYDevuelveElResultadoConLosParametrosPorDefecto() throws Exception {
        String body = alinear(mvc(FUENTE, 100), cuerpo(""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains("\"algorithm\":\"Needleman-Wunsch\""));
        assertTrue(body.contains("\"score\":7.0"));
        assertTrue(body.contains("\"alignedTarget\":\"AC-E\""));
        assertTrue(body.contains("\"identityPercent\":75.0"));
        assertTrue(body.contains("\"gapOpen\":10"));
        assertTrue(body.contains("\"id\":\"4HHB_1\""));
        assertTrue(body.contains("\"id\":\"4HHB_2\""));
        assertEquals("VLSPADK", recibida.get().query());
        assertEquals("VHLTPEEK", recibida.get().target());
    }

    @Test
    void usaLosParametrosIndicados() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo(",\"matrix\":\"BLOSUM62\",\"gapOpen\":12,\"gapExtension\":2"))
                .andExpect(status().isOk());

        assertEquals(12, recibida.get().parameters().gapOpen());
        assertEquals(2, recibida.get().parameters().gapExtension());
    }

    @Test
    void unaPenalizacionFueraDeRangoDa400() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo(",\"gapOpen\":500")).andExpect(status().isBadRequest());
    }

    @Test
    void unTipoDesconocidoDa400() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replace("GLOBAL", "SEMIGLOBAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unJsonRotoDa400() throws Exception {
        alinear(mvc(FUENTE, 100), "{esto no es json").andExpect(status().isBadRequest());
    }

    @Test
    void faltandoCamposObligatoriosDa400() throws Exception {
        alinear(mvc(FUENTE, 100), "{\"type\":\"GLOBAL\"}").andExpect(status().isBadRequest());
    }

    @Test
    void unCodigoPdbInvalidoDa400() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replaceFirst("4HHB", "0000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unaEntradaInexistenteDa404() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replaceFirst("4HHB", "9ZZX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unaEntidadInexistenteDa404() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replace("\"entityId\":2", "\"entityId\":9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unaEntidadQueNoEsProteinaDa404() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replace("\"entityId\":2", "\"entityId\":3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unaProteinaMasLargaQueElTopeDa422() throws Exception {
        alinear(mvc(FUENTE, 7), cuerpo("")).andExpect(status().is(422));
    }

    @Test
    void elAlineamientoLocalDa501MientrasNoEsteImplementado() throws Exception {
        alinear(mvc(FUENTE, 100), cuerpo("").replace("GLOBAL", "LOCAL"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void unRcsbCaidoDa503() throws Exception {
        PdbEntrySource caida = code -> {
            throw new PdbUnavailableException("sin red");
        };

        alinear(mvc(caida, 100), cuerpo("")).andExpect(status().isServiceUnavailable());
    }
}