package com.santiagolovisotto.proteinaligner.alignment;

public interface Aligner {

    AlignmentResult align(AlignmentRequest request);
}