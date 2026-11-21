package com.santiagolovisotto.proteinaligner.alignment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlignmentContractTest {

    private static AlignmentResult resultado(String q, String t, String linea, int identidades, int similitudes) {
        return new AlignmentResult(AlignmentType.GLOBAL, AlignmentParameters.defaults(), 7.0,
                q, t, linea, identidades, similitudes, new SequenceRange(1, 4), new SequenceRange(1, 3));
    }

    @Test
    void laSolicitudNormalizaLasSecuencias() {
        AlignmentRequest request = new AlignmentRequest(" acde ", "ace", AlignmentType.GLOBAL,
                AlignmentParameters.defaults());

        assertEquals("ACDE", request.query());
        assertEquals("ACE", request.target());
    }

    @Test
    void laSolicitudRechazaSecuenciasInvalidas() {
        AlignmentParameters p = AlignmentParameters.defaults();
        assertThrows(IllegalArgumentException.class, () -> new AlignmentRequest("", "ACE", AlignmentType.GLOBAL, p));
        assertThrows(IllegalArgumentException.class, () -> new AlignmentRequest(null, "ACE", AlignmentType.GLOBAL, p));
        assertThrows(IllegalArgumentException.class, () -> new AlignmentRequest("AC-E", "ACE", AlignmentType.GLOBAL, p));
        assertThrows(IllegalArgumentException.class, () -> new AlignmentRequest("ACDE", "AC E", AlignmentType.GLOBAL, p));
    }

    @Test
    void losParametrosPorDefectoSonBlosum62Con10Y1() {
        AlignmentParameters p = AlignmentParameters.defaults();

        assertEquals(SubstitutionMatrixType.BLOSUM62, p.matrix());
        assertEquals(10, p.gapOpen());
        assertEquals(1, p.gapExtension());
    }

    @Test
    void losParametrosRechazanPenalizacionesFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> new AlignmentParameters(SubstitutionMatrixType.BLOSUM62, -1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new AlignmentParameters(SubstitutionMatrixType.BLOSUM62, 10, 101));
    }

    @Test
    void elResultadoCalculaLasMetricasDerivadas() {
        AlignmentResult r = resultado("ACDE", "AC-E", "|| |", 3, 3);

        assertEquals(4, r.length());
        assertEquals(1, r.gaps());
        assertEquals(75.0, r.identityPercent());
        assertEquals(75.0, r.similarityPercent());
    }

    @Test
    void elResultadoRechazaAlineamientosDeDistintoLargo() {
        assertThrows(IllegalArgumentException.class, () -> resultado("ACDE", "AC-", "|| |", 3, 3));
        assertThrows(IllegalArgumentException.class, () -> resultado("ACDE", "AC-E", "|| ", 3, 3));
    }

    @Test
    void elResultadoRechazaContadoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> resultado("ACDE", "AC-E", "|| |", 5, 3));
        assertThrows(IllegalArgumentException.class, () -> resultado("ACDE", "AC-E", "|| |", -1, 3));
        assertThrows(IllegalArgumentException.class, () -> resultado("ACDE", "AC-E", "|| |", 3, 2));
    }

    @Test
    void elRangoUsaPosicionesDesdeUnoYRechazaLosInvalidos() {
        assertEquals(4, new SequenceRange(1, 4).length());
        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(0, 4));
        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(5, 4));
    }
}