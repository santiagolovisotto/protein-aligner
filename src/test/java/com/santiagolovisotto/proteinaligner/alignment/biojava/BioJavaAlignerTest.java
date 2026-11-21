package com.santiagolovisotto.proteinaligner.alignment.biojava;

import com.santiagolovisotto.proteinaligner.alignment.AlignmentParameters;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentRequest;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentResult;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentType;
import com.santiagolovisotto.proteinaligner.alignment.SequenceRange;
import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BioJavaAlignerTest {

    private final BioJavaAligner aligner = new BioJavaAligner();

    private AlignmentResult global(String query, String target) {
        return aligner.align(new AlignmentRequest(query, target, AlignmentType.GLOBAL, AlignmentParameters.defaults()));
    }

    @Test
    void unaSecuenciaContraSiMismaSumaLaDiagonal() {
        AlignmentResult r = global("ACDE", "ACDE");

        assertEquals(24.0, r.score());
        assertEquals("ACDE", r.alignedQuery());
        assertEquals("ACDE", r.alignedTarget());
        assertEquals("||||", r.matchLine());
        assertEquals(4, r.identities());
        assertEquals(100.0, r.identityPercent());
        assertEquals(0, r.gaps());
    }

    // A-A 4 + C-C 9 + E-E 5 = 18, menos un gap de largo 1 (10 + 1) = 7.
    @Test
    void unGapDeLargoUnoSeCobraAperturaMasExtension() {
        AlignmentResult r = global("ACDE", "ACE");

        assertEquals(7.0, r.score());
        assertEquals("ACDE", r.alignedQuery());
        assertEquals("AC-E", r.alignedTarget());
        assertEquals("|| |", r.matchLine());
        assertEquals(3, r.identities());
        assertEquals(3, r.similarities());
        assertEquals(1, r.gaps());
        assertEquals(75.0, r.identityPercent());
        assertEquals(new SequenceRange(1, 4), r.queryRange());
        assertEquals(new SequenceRange(1, 3), r.targetRange());
    }

    // A 4 + C 9 + F 6 = 19, menos un gap de largo 2 (10 + 2) = 7.
    // Con un gap de largo 1 apertura y extension son intercambiables (10+1 = 1+10);
    // con largo 2 no (12 contra 21), asi que este caso detecta un cruce de parametros.
    @Test
    void unGapDeLargoDosSeCobraAperturaMasDosExtensiones() {
        AlignmentResult r = global("ACDEF", "ACF");

        assertEquals(7.0, r.score());
        assertEquals("AC--F", r.alignedTarget());
        assertEquals("||  |", r.matchLine());
        assertEquals(2, r.gaps());
    }

    // K contra R vale 2 en BLOSUM62: no es identico pero si similar. 4 + 2 + 6 = 12.
    @Test
    void unaSustitucionConservativaCuentaComoSimilar() {
        AlignmentResult r = global("AKD", "ARD");

        assertEquals(12.0, r.score());
        assertEquals("|:|", r.matchLine());
        assertEquals(2, r.identities());
        assertEquals(3, r.similarities());
    }

    // W contra G vale -2: ni identico ni similar. 4 - 2 + 6 = 8.
    @Test
    void unaSustitucionDesfavorableNoCuentaComoSimilar() {
        AlignmentResult r = global("AWD", "AGD");

        assertEquals(8.0, r.score());
        assertEquals("| |", r.matchLine());
        assertEquals(2, r.identities());
        assertEquals(2, r.similarities());
    }

    // Contraste con los contadores propios de BioJava: confirma que usamos su misma definicion.
    @Test
    void lasIdentidadesYSimilitudesCoincidenConLasDeBioJava() throws Exception {
        String query = "HEAGAWGHEE";
        String target = "PAWHEAE";

        AlignmentResult nuestro = global(query, target);
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(
                new ProteinSequence(query), new ProteinSequence(target),
                PairwiseSequenceAlignerType.GLOBAL,
                new SimpleGapPenalty(),
                SubstitutionMatrixHelper.getBlosum62());

        assertEquals(pair.getNumIdenticals(), nuestro.identities());
        assertEquals(pair.getNumSimilars(), nuestro.similarities());
    }

    // Los PDB reales traen a veces X (residuo desconocido): BioJava tiene que tolerarlo.
    @Test
    void toleraResiduosDesconocidos() {
        assertDoesNotThrow(() -> global("ACXDE", "ACDE"));
    }

    @Test
    void elAlineamientoLocalTodaviaNoEstaImplementado() {
        AlignmentRequest request = new AlignmentRequest("ACDE", "ACE", AlignmentType.LOCAL,
                AlignmentParameters.defaults());

        assertThrows(UnsupportedOperationException.class, () -> aligner.align(request));
    }
}