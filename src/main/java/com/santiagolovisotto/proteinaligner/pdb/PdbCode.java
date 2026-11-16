package com.santiagolovisotto.proteinaligner.pdb;

import java.util.Locale;
import java.util.regex.Pattern;

public record PdbCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("[1-9][A-Z0-9]{3}");

    public PdbCode {
        if (value == null) {
            throw new InvalidPdbCodeException("El codigo PDB es obligatorio");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidPdbCodeException(
                    "Codigo PDB invalido: deben ser 4 caracteres alfanumericos y el primero un digito del 1 al 9");
        }
    }
}