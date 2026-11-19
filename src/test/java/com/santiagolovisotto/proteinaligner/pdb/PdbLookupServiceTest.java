package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdbLookupServiceTest {

    private static final PdbCode CODE = new PdbCode("1TUP");

    private static PdbEntity entity(int id, String sequence) {
        return new PdbEntity(CODE, id, List.of("A"), "ENTIDAD " + id, "Homo sapiens", sequence);
    }

    @Test
    void devuelveSoloLasProteinasDeUnaEntradaMixta() {
        PdbEntity proteina = entity(1, "MEEPQSDPSV");
        PdbEntity adn = entity(2, "ATGCATGCAT");
        PdbLookupService service = new PdbLookupService(code -> List.of(proteina, adn));

        assertEquals(List.of(proteina), service.findProteins(CODE));
    }

    @Test
    void rechazaUnaEntradaQueSoloTieneAcidosNucleicos() {
        PdbEntity adn = entity(1, "ATGCATGCAT");
        PdbEntity arn = entity(2, "AUGCAUGCAU");
        PdbLookupService service = new PdbLookupService(code -> List.of(adn, arn));

        assertThrows(NoProteinEntitiesException.class, () -> service.findProteins(CODE));
    }

    @Test
    void rechazaUnaEntradaSinEntidades() {
        PdbLookupService service = new PdbLookupService(code -> List.of());

        assertThrows(NoProteinEntitiesException.class, () -> service.findProteins(CODE));
    }

    @Test
    void propagaElErrorCuandoLaEntradaNoExiste() {
        PdbLookupService service = new PdbLookupService(code -> {
            throw new PdbEntryNotFoundException(code);
        });

        assertThrows(PdbEntryNotFoundException.class, () -> service.findProteins(CODE));
    }

    @Test
    void propagaElErrorCuandoLaFuenteNoEstaDisponible() {
        PdbLookupService service = new PdbLookupService(code -> {
            throw new PdbUnavailableException("sin red", new RuntimeException());
        });

        assertThrows(PdbUnavailableException.class, () -> service.findProteins(CODE));
    }
}