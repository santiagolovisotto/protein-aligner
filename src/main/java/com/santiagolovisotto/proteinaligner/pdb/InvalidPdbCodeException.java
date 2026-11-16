package com.santiagolovisotto.proteinaligner.pdb;

public class InvalidPdbCodeException extends IllegalArgumentException {

    public InvalidPdbCodeException(String message) {
        super(message);
    }
}