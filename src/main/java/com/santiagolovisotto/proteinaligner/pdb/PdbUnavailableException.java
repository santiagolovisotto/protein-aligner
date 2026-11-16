package com.santiagolovisotto.proteinaligner.pdb;

public class PdbUnavailableException extends RuntimeException {

    public PdbUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}