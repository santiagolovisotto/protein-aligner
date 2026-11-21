package com.santiagolovisotto.proteinaligner.alignment;

import java.util.Objects;

/**
 * Penalizaciones positivas, como las pide BioJava: un gap de largo n cuesta
 * gapOpen + n * gapExtension.
 */
public record AlignmentParameters(SubstitutionMatrixType matrix, int gapOpen, int gapExtension) {

    public static final int MAX_GAP_PENALTY = 100;

    public AlignmentParameters {
        Objects.requireNonNull(matrix, "La matriz de sustitucion es obligatoria");
        if (gapOpen < 0 || gapOpen > MAX_GAP_PENALTY) {
            throw new IllegalArgumentException("La penalizacion de apertura debe estar entre 0 y " + MAX_GAP_PENALTY);
        }
        if (gapExtension < 0 || gapExtension > MAX_GAP_PENALTY) {
            throw new IllegalArgumentException("La penalizacion de extension debe estar entre 0 y " + MAX_GAP_PENALTY);
        }
    }

    /** BLOSUM62 con los valores por defecto de BioJava (10 para abrir, 1 para extender). */
    public static AlignmentParameters defaults() {
        return new AlignmentParameters(SubstitutionMatrixType.BLOSUM62, 10, 1);
    }
}