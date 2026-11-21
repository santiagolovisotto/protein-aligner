package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.AlignmentParameters;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentResult;
import com.santiagolovisotto.proteinaligner.alignment.AlignmentType;
import com.santiagolovisotto.proteinaligner.alignment.SubstitutionMatrixType;

public record AlignmentResponse(
        String algorithm,
        AlignmentType type,
        SubstitutionMatrixType matrix,
        int gapOpen,
        int gapExtension,
        double score,
        String alignedQuery,
        String alignedTarget,
        String matchLine,
        int identities,
        int similarities,
        int gaps,
        int length,
        double identityPercent,
        double similarityPercent,
        int queryStart,
        int queryEnd,
        int targetStart,
        int targetEnd) {

    static AlignmentResponse from(AlignmentResult r) {
        AlignmentParameters p = r.parameters();
        return new AlignmentResponse(
                r.type().algorithmName(),
                r.type(),
                p.matrix(),
                p.gapOpen(),
                p.gapExtension(),
                r.score(),
                r.alignedQuery(),
                r.alignedTarget(),
                r.matchLine(),
                r.identities(),
                r.similarities(),
                r.gaps(),
                r.length(),
                round1(r.identityPercent()),
                round1(r.similarityPercent()),
                r.queryRange().start(),
                r.queryRange().end(),
                r.targetRange().start(),
                r.targetRange().end());
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}