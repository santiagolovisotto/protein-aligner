package com.santiagolovisotto.proteinaligner.alignment.biojava;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceAligner;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BioJavaCharacterizationTest {

    private static PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner(
            String query, String target, PairwiseSequenceAlignerType type) throws Exception {
        SubstitutionMatrix<AminoAcidCompound> blosum62 = SubstitutionMatrixHelper.getBlosum62();
        GapPenalty gaps = new SimpleGapPenalty();
        return Alignments.getPairwiseAligner(
                new ProteinSequence(query), new ProteinSequence(target), type, gaps, blosum62);
    }

    // En BLOSUM62 la diagonal vale A=4, C=9, D=6, E=5, asi que ACDE contra si misma suma 24.
    @Test
    void globalDeUnaSecuenciaIdenticaSumaLaDiagonalDeBlosum62() throws Exception {
        assertEquals(24.0, aligner("ACDE", "ACDE", PairwiseSequenceAlignerType.GLOBAL).getScore());
    }

    @Test
    void localDeUnaSecuenciaIdenticaDaElMismoScore() throws Exception {
        assertEquals(24.0, aligner("ACDE", "ACDE", PairwiseSequenceAlignerType.LOCAL).getScore());
    }

    // Un gap de largo 1 cuesta 11: 10 de apertura + 1 de extension (18 - 11 = 7).
    @Test
    void unGapDeLargoUnoCuestaAperturaMasExtension() throws Exception {
        assertEquals(7.0, aligner("ACDE", "ACE", PairwiseSequenceAlignerType.GLOBAL).getScore());
    }

    // Prediccion a confirmar: un gap de largo 2 cuesta 10 + 2 x 1 = 12 (A 4 + C 9 + F 6 = 19; 19 - 12 = 7).
    @Test
    void unGapDeLargoDosCuestaAperturaMasDosExtensiones() throws Exception {
        assertEquals(7.0, aligner("ACDEF", "ACF", PairwiseSequenceAlignerType.GLOBAL).getScore());
    }
}