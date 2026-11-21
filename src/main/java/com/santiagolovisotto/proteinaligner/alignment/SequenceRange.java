package com.santiagolovisotto.proteinaligner.alignment;

public record SequenceRange(int start, int end) {

    public SequenceRange {
        if (start < 1 || end < start) {
            throw new IllegalArgumentException("Rango invalido: " + start + "-" + end);
        }
    }

    public int length() {
        return end - start + 1;
    }
}