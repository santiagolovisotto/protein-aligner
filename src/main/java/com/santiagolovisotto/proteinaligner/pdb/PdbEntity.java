package com.santiagolovisotto.proteinaligner.pdb;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record PdbEntity(
        PdbCode code,
        int entityId,
        List<String> chains,
        String name,
        String organism,
        String sequence) {

    private static final Pattern LETTERS = Pattern.compile("[A-Z]+");
    private static final Pattern NUCLEOTIDES = Pattern.compile("[ACGTUN]+");

    public PdbEntity {
        Objects.requireNonNull(code, "El codigo PDB es obligatorio");
        if (chains == null || chains.isEmpty()) {
            throw new IllegalArgumentException("La entidad debe tener al menos una cadena");
        }
        chains = List.copyOf(chains);
        name = name == null ? "" : name.trim();
        organism = organism == null ? "" : organism.trim();
        if (sequence == null || sequence.isBlank()) {
            throw new IllegalArgumentException("La secuencia no puede estar vacia");
        }
        sequence = sequence.trim().toUpperCase(Locale.ROOT);
        if (!LETTERS.matcher(sequence).matches()) {
            throw new IllegalArgumentException("La secuencia solo puede contener letras");
        }
    }

    /** Identificador legible, por ejemplo 4HHB_1. */
    public String id() {
        return code.value() + "_" + entityId;
    }

    public int length() {
        return sequence.length();
    }


    /**
     * Heuristica: una secuencia hecha solo de A, C, G, T, U, N se considera
     * acido nucleico. Limite conocido: un peptido hecho solo de esas letras
     * se confundiria.
     */
    public boolean isProtein() {
        return !NUCLEOTIDES.matcher(sequence).matches();
    }
}