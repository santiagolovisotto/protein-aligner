package com.santiagolovisotto.proteinaligner.alignment.biojava;

import com.santiagolovisotto.proteinaligner.alignment.AlignmentRequest;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentResult;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentType;
import com.santiagolovisotto.proteinaligner.alignment.Aligner;
import com.santiagolovisotto.proteinaligner.alignment.NoSimilarRegionException;
import com.santiagolovisotto.proteinaligner.alignment.SequenceRange;
import com.santiagolovisotto.proteinaligner.alignment.SubstitutionMatrixType;
import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceAligner;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.springframework.stereotype.Component;

/**
 * Unico lugar del proyecto donde se usa BioJava. Traduce el contrato propio
 * (AlignmentRequest / AlignmentResult) a la libreria y viceversa.
 */
@Component
class BioJavaAligner implements Aligner {

    private static final String GAP = "-";

    @Override
    public AlignmentResult align(AlignmentRequest request) {
        SubstitutionMatrix<AminoAcidCompound> matrix = matrixFor(request.parameters().matrix());
        GapPenalty gaps = new SimpleGapPenalty(
                (short) request.parameters().gapOpen(),
                (short) request.parameters().gapExtension());

        PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner = Alignments.getPairwiseAligner(
                toProtein("query", request.query()),
                toProtein("target", request.target()),
                biojavaType(request.type()),
                gaps,
                matrix);

        double score = aligner.getScore();
        if (request.type() == AlignmentType.LOCAL && score <= 0) {
            throw new NoSimilarRegionException();
        }
        SequencePair<ProteinSequence, AminoAcidCompound> pair = aligner.getPair();

        StringBuilder alignedQuery = new StringBuilder();
        StringBuilder alignedTarget = new StringBuilder();
        StringBuilder matchLine = new StringBuilder();
        int identities = 0;
        int similarities = 0;

        for (int column = 1; column <= pair.getLength(); column++) {
            AminoAcidCompound q = pair.getCompoundInQueryAt(column);
            AminoAcidCompound t = pair.getCompoundInTargetAt(column);
            String queryLetter = q.getShortName();
            String targetLetter = t.getShortName();
            alignedQuery.append(queryLetter);
            alignedTarget.append(targetLetter);

            if (GAP.equals(queryLetter) || GAP.equals(targetLetter)) {
                matchLine.append(' ');
            } else if (queryLetter.equals(targetLetter)) {
                identities++;
                similarities++;
                matchLine.append('|');
            } else if (matrix.getValue(q, t) > 0) {
                similarities++;
                matchLine.append(':');
            } else {
                matchLine.append(' ');
            }
        }

        SequenceRange queryRange;
        SequenceRange targetRange;
        if (request.type() == AlignmentType.GLOBAL) {
            queryRange = new SequenceRange(1, request.query().length());
            targetRange = new SequenceRange(1, request.target().length());
        } else {
            // Un alineamiento local empieza y termina en columnas sin gap.
            int last = pair.getLength();
            queryRange = new SequenceRange(pair.getIndexInQueryAt(1), pair.getIndexInQueryAt(last));
            targetRange = new SequenceRange(pair.getIndexInTargetAt(1), pair.getIndexInTargetAt(last));
        }

        return new AlignmentResult(
                request.type(),
                request.parameters(),
                score,
                alignedQuery.toString(),
                alignedTarget.toString(),
                matchLine.toString(),
                identities,
                similarities,
                queryRange,
                targetRange);
    }

    private static PairwiseSequenceAlignerType biojavaType(AlignmentType type) {
        return switch (type) {
            case GLOBAL -> PairwiseSequenceAlignerType.GLOBAL;
            case LOCAL -> PairwiseSequenceAlignerType.LOCAL;
        };
    }

    private static SubstitutionMatrix<AminoAcidCompound> matrixFor(SubstitutionMatrixType type) {
        return switch (type) {
            case BLOSUM62 -> SubstitutionMatrixHelper.getBlosum62();
        };
    }

    private static ProteinSequence toProtein(String name, String sequence) {
        try {
            return new ProteinSequence(sequence);
        } catch (CompoundNotFoundException e) {
            throw new IllegalArgumentException(
                    "La secuencia " + name + " tiene caracteres que no son aminoacidos", e);
        }
    }
}