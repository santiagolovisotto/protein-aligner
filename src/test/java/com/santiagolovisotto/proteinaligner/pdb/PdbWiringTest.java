package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "app.pdb.base-url=http://localhost:1")
class PdbWiringTest {

    @Autowired
    private PdbLookupService service;

    @Test
    void elServicioUsaLaUrlConfiguradaYTraduceElFalloDeRed() {
        assertThrows(PdbUnavailableException.class,
                () -> service.findProteins(new PdbCode("4HHB")));
    }
}