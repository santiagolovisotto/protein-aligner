package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdbCodeTest {

    @Test
    void aceptaCodigoValidoYLoNormalizaAMayusculas() {
        assertEquals("4HHB", new PdbCode(" 4hhb ").value());
    }

    @Test
    void rechazaCodigosConFormatoInvalido() {
        List<String> invalidos = List.of("", "4HH", "4HHBB", "0ABC", "4H-B", "ABCD", "   ");
        for (String codigo : invalidos) {
            assertThrows(InvalidPdbCodeException.class, () -> new PdbCode(codigo),
                    "Deberia rechazar: '" + codigo + "'");
        }
    }

    @Test
    void rechazaCodigoNulo() {
        assertThrows(InvalidPdbCodeException.class, () -> new PdbCode(null));
    }
}