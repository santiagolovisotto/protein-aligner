package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdbEntityTest {

    private static final PdbCode CODE = new PdbCode("4HHB");

    private PdbEntity entity(List<String> chains, String sequence) {
        return new PdbEntity(CODE, 1, chains, "HEMOGLOBINA", "Homo sapiens", sequence);
    }

    @Test
    void construyeEntidadValidaYNormalizaLaSecuencia() {
        PdbEntity e = entity(List.of("A", "C"), " vlspadk ");
        assertEquals("VLSPADK", e.sequence());
        assertEquals(7, e.length());
        assertEquals("4HHB_1", e.id());
    }

    @Test
    void rechazaSecuenciaVaciaOConCaracteresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> entity(List.of("A"), "  "));
        assertThrows(IllegalArgumentException.class, () -> entity(List.of("A"), "VL-SP"));
        assertThrows(IllegalArgumentException.class, () -> entity(List.of("A"), "VL SP"));
    }

    @Test
    void rechazaEntidadSinCadenas() {
        assertThrows(IllegalArgumentException.class, () -> entity(List.of(), "VLSPADK"));
    }

    @Test
    void laListaDeCadenasEsInmutable() {
        List<String> original = new ArrayList<>(List.of("A"));
        PdbEntity e = entity(original, "VLSPADK");
        original.add("B");
        assertEquals(List.of("A"), e.chains());
    }

    @Test
    void distingueProteinasDeAcidosNucleicos() {
        assertEquals(true, entity(List.of("A"), "VLSPADK").isProtein());
        assertEquals(false, entity(List.of("A"), "ACGTACGT").isProtein());
        assertEquals(false, entity(List.of("A"), "ACGUNACGU").isProtein());
    }
}