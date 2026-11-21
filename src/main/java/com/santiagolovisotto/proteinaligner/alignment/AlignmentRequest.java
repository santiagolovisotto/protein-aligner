package com.santiagolovisotto.proteinaligner.alignment;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record AlignmentRequest(
        String query,
        String target,
        AlignmentType type,
        AlignmentParameters parameters) {

    private static final Pattern LETTERS = Pattern.compile("[A-Z]+");

    public AlignmentRequest {
        query = normalize("query", query);
        target = normalize("target", target);
        Objects.requireNonNull(type, "El tipo de alineamiento es obligatorio");
        Objects.requireNonNull(parameters, "Los parametros son obligatorios");
    }

    private static String normalize(String name, String sequence) {
        if (sequence == null || sequence.isBlank()) {
            throw new IllegalArgumentException("La secuencia " + name + " no puede estar vacia");
        }
        String normalized = sequence.trim().toUpperCase(Locale.ROOT);
        if (!LETTERS.matcher(normalized).matches()) {
            throw new IllegalArgumentException("La secuencia " + name + " solo puede contener letras");
        }
        return normalized;
    }
}