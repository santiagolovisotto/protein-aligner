package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CachingPdbEntrySourceTest {

    private static final PdbCode A = new PdbCode("1AAA");
    private static final PdbCode B = new PdbCode("2BBB");
    private static final PdbCode C = new PdbCode("3CCC");

    private static class FuenteContadora implements PdbEntrySource {
        int llamadas;

        @Override
        public List<PdbEntity> fetchEntities(PdbCode code) {
            llamadas++;
            return List.of(new PdbEntity(code, 1, List.of("A"), "Prueba", "Homo sapiens", "VLSPADK"));
        }
    }

    @Test
    void laSegundaConsultaDelMismoCodigoNoVuelveAlaFuente() {
        FuenteContadora fuente = new FuenteContadora();
        CachingPdbEntrySource cache = new CachingPdbEntrySource(fuente, 10);

        List<PdbEntity> primera = cache.fetchEntities(A);
        List<PdbEntity> segunda = cache.fetchEntities(A);

        assertEquals(1, fuente.llamadas);
        assertSame(primera, segunda);
    }

    @Test
    void codigosDistintosSeConsultanPorSeparado() {
        FuenteContadora fuente = new FuenteContadora();
        CachingPdbEntrySource cache = new CachingPdbEntrySource(fuente, 10);

        cache.fetchEntities(A);
        cache.fetchEntities(B);

        assertEquals(2, fuente.llamadas);
    }

    @Test
    void losErroresNoSeGuardan() {
        int[] llamadas = {0};
        PdbEntrySource fuenteInestable = code -> {
            llamadas[0]++;
            if (llamadas[0] == 1) {
                throw new PdbUnavailableException("sin red");
            }
            return List.of(new PdbEntity(code, 1, List.of("A"), "Prueba", "Homo sapiens", "VLSPADK"));
        };
        CachingPdbEntrySource cache = new CachingPdbEntrySource(fuenteInestable, 10);

        assertThrows(PdbUnavailableException.class, () -> cache.fetchEntities(A));
        assertEquals(1, cache.fetchEntities(A).size());
        assertEquals(2, llamadas[0]);
    }

    @Test
    void descartaLaEntradaQueHaceMasTiempoQueNadieUsa() {
        FuenteContadora fuente = new FuenteContadora();
        CachingPdbEntrySource cache = new CachingPdbEntrySource(fuente, 2);

        cache.fetchEntities(A); // miss (1)
        cache.fetchEntities(B); // miss (2)
        cache.fetchEntities(A); // hit: A pasa a ser la mas reciente
        cache.fetchEntities(C); // miss (3): desaloja a B, la menos usada
        cache.fetchEntities(A); // hit: A sigue en la cache
        cache.fetchEntities(B); // miss (4): B habia sido desalojada

        assertEquals(4, fuente.llamadas);
    }

    @Test
    void rechazaUnTamanoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new CachingPdbEntrySource(new FuenteContadora(), 0));
    }
}