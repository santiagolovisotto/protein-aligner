package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.pdb.PdbCode;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntryNotFoundException;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntrySource;
import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;
import com.santiagolovisotto.proteinaligner.pdb.PdbUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PdbControllerTest {

    private static MockMvc mvc(PdbEntrySource source) {
        return MockMvcBuilders
                .standaloneSetup(new PdbController(new PdbLookupService(source)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static PdbEntity entity(PdbCode code, int id, String name, String sequence) {
        return new PdbEntity(code, id, List.of("A"), name, "Homo sapiens (9606)", sequence);
    }

    @Test
    void devuelveLasProteinasYDescartaElAdn() throws Exception {
        PdbEntrySource source = code -> List.of(
                entity(code, 1, "Hemoglobin subunit alpha", "VLSPADK"),
                entity(code, 2, "DNA de prueba", "ATGCATGC"));

        String body = mvc(source).perform(get("/api/pdb/4hhb"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains("\"id\":\"4HHB_1\""));
        assertTrue(body.contains("\"length\":7"));
        assertFalse(body.contains("4HHB_2"));
    }

    @Test
    void unCodigoInvalidoDa400() throws Exception {
        mvc(code -> List.of()).perform(get("/api/pdb/0000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unaEntradaInexistenteDa404() throws Exception {
        PdbEntrySource source = code -> {
            throw new PdbEntryNotFoundException(code);
        };

        mvc(source).perform(get("/api/pdb/9ZZX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unaEntradaSinProteinasDa422() throws Exception {
        PdbEntrySource source = code -> List.of(entity(code, 1, "DNA de prueba", "ATGCATGC"));

        mvc(source).perform(get("/api/pdb/1ABC"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unRcsbCaidoDa503() throws Exception {
        PdbEntrySource source = code -> {
            throw new PdbUnavailableException("sin red");
        };

        mvc(source).perform(get("/api/pdb/4HHB"))
                .andExpect(status().isServiceUnavailable());
    }
}