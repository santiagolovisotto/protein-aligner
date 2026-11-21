package com.santiagolovisotto.proteinaligner.alignment;

import com.santiagolovisotto.proteinaligner.pdb.PdbEntity;

public record ProteinComparison(PdbEntity query, PdbEntity target, AlignmentResult result) {
}