package com.santiagolovisotto.proteinaligner.alignment;

import com.santiagolovisotto.proteinaligner.pdb.PdbCode;

public record ProteinReference(PdbCode code, int entityId) {
}