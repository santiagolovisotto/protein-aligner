package com.santiagolovisotto.proteinaligner.alignment;

import com.santiagolovisotto.proteinaligner.pdb.PdbCode;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;
import com.santiagolovisotto.proteinaligner.pdb.PdbEntryNotFoundException;
import com.santiagolovisotto.proteinaligner.pdb.PdbLookupService;
import com.santiagolovisotto.proteinaligner.pdb.ProteinEntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlignProteinsServiceTest {

    private static final PdbCode HEMOGLOBINA = new PdbCode("4HHB");
    private static final AlignmentParameters PARAMETROS = AlignmentParameters.defaults();

    private static final AlignmentResult RESULTADO = new AlignmentResult(
            AlignmentType.GLOBAL, PARAMETROS, 1.0, "A", "A", "|", 1, 1,
            new SequenceRange(1, 1), new SequenceRange(1, 1));

    private final AtomicReference<AlignmentRequest> recibida = new AtomicReference<>();

    private final Aligner alineadorFalso = request -> {
        recibida.set(request);
        return RESULTADO;
    };

    private static PdbEntity entidad(PdbCode code, int id, String secuencia) {
        return new PdbEntity(code, id, List.of("A"), "Entidad " + id, "Homo sapiens (9606)", secuencia);
    }

    private AlignProteinsService servicio(List<PdbEntity> entidades, int maximo) {
        PdbLookupService pdb = new PdbLookupService(code -> entidades);
        return new AlignProteinsService(pdb, alineadorFalso, maximo);
    }

    private final List<PdbEntity> hemoglobina = List.of(
            entidad(HEMOGLOBINA, 1, "VLSPADK"),
            entidad(HEMOGLOBINA, 2, "VHLTPEEK"),
            entidad(HEMOGLOBINA, 3, "ATGCATGC"));

    @Test
    void alineaLasSecuenciasDeLasEntidadesPedidas() {
        AlignProteinsService servicio = servicio(hemoglobina, 100);

        ProteinComparison comparacion = servicio.compare(
                new ProteinReference(HEMOGLOBINA, 1),
                new ProteinReference(HEMOGLOBINA, 2),
                AlignmentType.GLOBAL, PARAMETROS);

        assertEquals("VLSPADK", recibida.get().query());
        assertEquals("VHLTPEEK", recibida.get().target());
        assertEquals(AlignmentType.GLOBAL, recibida.get().type());
        assertSame(PARAMETROS, recibida.get().parameters());
        assertEquals("4HHB_1", comparacion.query().id());
        assertEquals("4HHB_2", comparacion.target().id());
        assertSame(RESULTADO, comparacion.result());
    }

    @Test
    void sePuedeCompararUnaProteinaConsigoMisma() {
        AlignProteinsService servicio = servicio(hemoglobina, 100);

        servicio.compare(new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 1),
                AlignmentType.GLOBAL, PARAMETROS);

        assertEquals(recibida.get().query(), recibida.get().target());
    }

    @Test
    void rechazaUnaEntidadQueNoExiste() {
        AlignProteinsService servicio = servicio(hemoglobina, 100);

        assertThrows(ProteinEntityNotFoundException.class, () -> servicio.compare(
                new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 9),
                AlignmentType.GLOBAL, PARAMETROS));
        assertNull(recibida.get());
    }

    @Test
    void rechazaUnaEntidadQueNoEsProteina() {
        AlignProteinsService servicio = servicio(hemoglobina, 100);

        assertThrows(ProteinEntityNotFoundException.class, () -> servicio.compare(
                new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 3),
                AlignmentType.GLOBAL, PARAMETROS));
    }

    @Test
    void rechazaUnaProteinaMasLargaQueElTopeSinAlinear() {
        AlignProteinsService servicio = servicio(hemoglobina, 7);

        // La entidad 2 tiene 8 residuos y el tope es 7.
        assertThrows(SequenceTooLongException.class, () -> servicio.compare(
                new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 2),
                AlignmentType.GLOBAL, PARAMETROS));
        assertNull(recibida.get());
    }

    @Test
    void aceptaUnaProteinaJustoEnElTope() {
        AlignProteinsService servicio = servicio(hemoglobina, 8);

        servicio.compare(new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 2),
                AlignmentType.GLOBAL, PARAMETROS);

        assertEquals("VHLTPEEK", recibida.get().target());
    }

    @Test
    void propagaElErrorCuandoLaEntradaNoExiste() {
        PdbLookupService pdb = new PdbLookupService(code -> {
            throw new PdbEntryNotFoundException(code);
        });
        AlignProteinsService servicio = new AlignProteinsService(pdb, alineadorFalso, 100);

        assertThrows(PdbEntryNotFoundException.class, () -> servicio.compare(
                new ProteinReference(HEMOGLOBINA, 1), new ProteinReference(HEMOGLOBINA, 2),
                AlignmentType.GLOBAL, PARAMETROS));
    }

    @Test
    void rechazaUnTopeInvalido() {
        PdbLookupService pdb = new PdbLookupService(code -> hemoglobina);

        assertThrows(IllegalArgumentException.class,
                () -> new AlignProteinsService(pdb, alineadorFalso, 0));
    }
}