package com.santiagolovisotto.proteinaligner.alignment;

public class SequenceTooLongException extends RuntimeException {

    public SequenceTooLongException(String proteinId, int length, int maxLength) {
        super("La proteina " + proteinId + " tiene " + length
                + " residuos y el maximo permitido es " + maxLength);
    }
}