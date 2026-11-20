package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdbRealResponseTest {

    private static final PdbCode NUCLEOSOMA = new PdbCode("9ZZZ");

    // Encabezados reales de RCSB, en su orden original (no numerico). Secuencias recortadas.
    private static final String RESPUESTA_9ZZZ = """
            >9ZZZ_1|Chains A, B|Histone H2A|Drosophila melanogaster (7227)
            MSGRGKGGKVKGKAKSRSNR
            >9ZZZ_3|Chains E, F|Histone H3|Drosophila melanogaster (7227)
            MARTKQTARKSTGGKAPRKQ
            >9ZZZ_6|Chain J[auth L]|DNA 23-mer|synthetic construct (32630)
            GTCAGATATATACATCCTGTGAT
            >9ZZZ_7|Chain K[auth M]|DNA 153-mer|synthetic construct (32630)
            ATCACAGGATGTATATATCTGACGGAGACC
            >9ZZZ_8|Chain L[auth P]|Poly [ADP-ribose] polymerase 2|Homo sapiens (9606)
            GKAPVDPECTAKVGKAHVYC
            >9ZZZ_9|Chains M[auth I], N[auth J]|PL2-6 scFv|Mus musculus (10090)
            MGSSHHHHHHSSGRENLYFQ
            >9ZZZ_5|Chain I[auth K]|DNA 130-mer|synthetic construct (32630)
            ATCCTGGAGAATCCCGGTGCCGAGGCC
            >9ZZZ_4|Chains G, H|Histone H4|Drosophila melanogaster (7227)
            MITGRGKGGKGLGKGGAKRH
            >9ZZZ_2|Chains C, D|Histone H2B|Drosophila melanogaster (7227)
            GMIPPKTSGKAAKKAGKAQK
            """;

    @Test
    void ordenaLasEntidadesPorNumero() {
        List<Integer> ids = RcsbFastaParser.parse(NUCLEOSOMA, RESPUESTA_9ZZZ).stream()
                .map(PdbEntity::entityId)
                .toList();

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), ids);
    }

    @Test
    void conservaLasCadenasConIdentificadorDeAutor() {
        List<PdbEntity> entidades = RcsbFastaParser.parse(NUCLEOSOMA, RESPUESTA_9ZZZ);

        assertEquals(List.of("J[auth L]"), entidades.get(5).chains());
        assertEquals(List.of("M[auth I]", "N[auth J]"), entidades.get(8).chains());
    }

    @Test
    void elServicioSeQuedaSoloConLasProteinas() {
        PdbLookupService servicio =
                new PdbLookupService(code -> RcsbFastaParser.parse(code, RESPUESTA_9ZZZ));

        List<Integer> ids = servicio.findProteins(NUCLEOSOMA).stream()
                .map(PdbEntity::entityId)
                .toList();

        assertEquals(List.of(1, 2, 3, 4, 8, 9), ids);
    }
}