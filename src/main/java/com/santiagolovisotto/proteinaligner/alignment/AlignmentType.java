package com.santiagolovisotto.proteinaligner.alignment;

public enum AlignmentType {
    GLOBAL("Needleman-Wunsch"),
    LOCAL("Smith-Waterman");

    private final String algorithmName;

    AlignmentType(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String algorithmName() {
        return algorithmName;
    }
}