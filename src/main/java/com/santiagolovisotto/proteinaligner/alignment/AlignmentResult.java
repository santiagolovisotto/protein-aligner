package com.santiagolovisotto.proteinaligner.alignment;

import java.util.Objects;

/**
 * Los porcentajes se calculan sobre el largo del alineamiento (columnas,
 * contando los gaps), como hace EMBOSS Needle.
 */
public record AlignmentResult(
        AlignmentType type,
        AlignmentParameters parameters,
        double score,
        String alignedQuery,
        String alignedTarget,
        String matchLine,
        int identities,
        int similarities,
        SequenceRange queryRange,
        SequenceRange targetRange) {

    public AlignmentResult {
        Objects.requireNonNull(type, "El tipo es obligatorio");
        Objects.requireNonNull(parameters, "Los parametros son obligatorios");
        Objects.requireNonNull(alignedQuery, "El alineamiento de query es obligatorio");
        Objects.requireNonNull(alignedTarget, "El alineamiento de target es obligatorio");
        Objects.requireNonNull(matchLine, "La linea de coincidencias es obligatoria");
        Objects.requireNonNull(queryRange, "El rango de query es obligatorio");
        Objects.requireNonNull(targetRange, "El rango de target es obligatorio");
        if (alignedQuery.isEmpty()
                || alignedQuery.length() != alignedTarget.length()
                || alignedQuery.length() != matchLine.length()) {
            throw new IllegalArgumentException(
                    "Las dos secuencias alineadas y la linea de coincidencias deben tener el mismo largo y no estar vacias");
        }
        if (identities < 0 || identities > alignedQuery.length()
                || similarities < 0 || similarities > alignedQuery.length()) {
            throw new IllegalArgumentException("Identidades o similitudes fuera de rango");
        }
        if (similarities < identities) {
            throw new IllegalArgumentException("Las similitudes incluyen a las identidades: no pueden ser menos");
        }
    }

    public int length() {
        return alignedQuery.length();
    }

    /** Posiciones con gap, en cualquiera de las dos secuencias. */
    public int gaps() {
        int count = 0;
        for (int i = 0; i < alignedQuery.length(); i++) {
            if (alignedQuery.charAt(i) == '-' || alignedTarget.charAt(i) == '-') {
                count++;
            }
        }
        return count;
    }

    public double identityPercent() {
        return 100.0 * identities / length();
    }

    public double similarityPercent() {
        return 100.0 * similarities / length();
    }
}