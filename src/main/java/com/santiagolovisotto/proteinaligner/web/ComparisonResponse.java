package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.ProteinComparison;

public record ComparisonResponse(
        PdbEntityResponse query,
        PdbEntityResponse target,
        AlignmentResponse alignment) {

    static ComparisonResponse from(ProteinComparison comparison) {
        return new ComparisonResponse(
                PdbEntityResponse.from(comparison.query()),
                PdbEntityResponse.from(comparison.target()),
                AlignmentResponse.from(comparison.result()));
    }
}