package com.santiagolovisotto.proteinaligner.pdb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RcsbFastaParserTest {

    private static final PdbCode HEMOGLOBINA = new PdbCode("4HHB");

    private static final String RESPUESTA_REAL_4HHB = """
            >4HHB_1|Chains A, C|Hemoglobin subunit alpha|Homo sapiens (9606)
            VLSPADKTNVKAAWGKVGAHAGEYGAEALERMFLSFPTTKTYFPHFDLSHGSAQVKGHGKKVADALTNAVAHVDDMPNALSALSDLHAHKLRVDPVNFKLLSHCLLVTLAAHLPAEFTPAVHASLDKFLASVSTVLTSKYR
            >4HHB_2|Chains B, D|Hemoglobin subunit beta|Homo sapiens (9606)
            VHLTPEEKSAVTALWGKVNVDEVGGEALGRLLVVYPWTQRFFESFGDLSTPDAVMGNPKVKAHGKKVLGAFSDGLAHLDNLKGTFATLSELHCDKLHVDPENFRLLGNVLVCVLAHHFGKEFTPPVQAAYQKVVAGVANALAHKYH
            """;

    @Test
    void parseaLasDosEntidadesDeLaRespuestaReal() {
        List<PdbEntity> entidades = RcsbFastaParser.parse(HEMOGLOBINA, RESPUESTA_REAL_4HHB);

        assertEquals(2, entidades.size());

        PdbEntity alfa = entidades.get(0);
        assertEquals(1, alfa.entityId());
        assertEquals(List.of("A", "C"), alfa.chains());
        assertEquals("Hemoglobin subunit alpha", alfa.name());
        assertEquals("Homo sapiens (9606)", alfa.organism());
        assertTrue(alfa.sequence().startsWith("VLSPADK"));
        assertTrue(alfa.sequence().endsWith("KYR"));

        PdbEntity beta = entidades.get(1);
        assertEquals(2, beta.entityId());
        assertEquals(List.of("B", "D"), beta.chains());
        assertTrue(beta.sequence().startsWith("VHLTPEEK"));
        assertTrue(beta.sequence().endsWith("AHKYH"));
    }

    @Test
    void unaLasLineasCuandoLaSecuenciaVieneCortada() {
        String fasta = ">4HHB_1|Chains A|Prueba|Homo sapiens (9606)\nVLSPA\nDKTNV\n";

        List<PdbEntity> entidades = RcsbFastaParser.parse(HEMOGLOBINA, fasta);

        assertEquals("VLSPADKTNV", entidades.get(0).sequence());
    }

    @Test
    void aceptaFinesDeLineaDeWindows() {
        String fasta = ">4HHB_1|Chains A|Prueba|Homo sapiens (9606)\r\nVLSPADK\r\n";

        assertEquals("VLSPADK", RcsbFastaParser.parse(HEMOGLOBINA, fasta).get(0).sequence());
    }

    @Test
    void aceptaUnaSolaCadenaEnSingular() {
        String fasta = ">4HHB_1|Chain A|Prueba|Homo sapiens (9606)\nVLSPADK\n";

        assertEquals(List.of("A"), RcsbFastaParser.parse(HEMOGLOBINA, fasta).get(0).chains());
    }

    @Test
    void conservaLasCadenasConIdentificadorDeAutor() {
        String fasta = ">4HHB_1|Chains A[auth H], B[auth L]|Prueba|Homo sapiens (9606)\nVLSPADK\n";

        assertEquals(List.of("A[auth H]", "B[auth L]"),
                RcsbFastaParser.parse(HEMOGLOBINA, fasta).get(0).chains());
    }

    @Test
    void aceptaOrganismoVacio() {
        String fasta = ">4HHB_1|Chains A|Prueba|\nVLSPADK\n";

        assertEquals("", RcsbFastaParser.parse(HEMOGLOBINA, fasta).get(0).organism());
    }

    @Test
    void unaRespuestaVaciaNoTieneEntidades() {
        assertEquals(List.of(), RcsbFastaParser.parse(HEMOGLOBINA, ""));
    }

    @Test
    void rechazaRespuestasConFormatoInesperado() {
        assertThrows(PdbUnavailableException.class,
                () -> RcsbFastaParser.parse(HEMOGLOBINA, "VLSPADK"));
        assertThrows(PdbUnavailableException.class,
                () -> RcsbFastaParser.parse(HEMOGLOBINA, ">4HHB_1|Chains A\nVLSPADK"));
        assertThrows(PdbUnavailableException.class,
                () -> RcsbFastaParser.parse(HEMOGLOBINA, ">1ABC_1|Chains A|Prueba|Homo sapiens (9606)\nVLSPADK"));
        assertThrows(PdbUnavailableException.class,
                () -> RcsbFastaParser.parse(HEMOGLOBINA, ">4HHB_1|Chains A|Prueba|Homo sapiens (9606)\nVL-SP"));
    }
}