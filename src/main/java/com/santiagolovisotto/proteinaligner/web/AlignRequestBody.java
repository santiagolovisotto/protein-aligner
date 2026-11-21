package com.santiagolovisotto.proteinaligner.web;

import com.santiagolovisotto.proteinaligner.alignment.AlignmentType;
import com.santiagolovisotto.proteinaligner.alignment.SubstitutionMatrixType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlignRequestBody(
        @NotNull @Valid ProteinRefBody query,
        @NotNull @Valid ProteinRefBody target,
        @NotNull AlignmentType type,
        SubstitutionMatrixType matrix,
        Integer gapOpen,
        Integer gapExtension) {

    public record ProteinRefBody(
            @NotBlank String code,
            @NotNull @Min(1) Integer entityId) {
    }
}