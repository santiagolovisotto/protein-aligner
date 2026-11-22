package com.santiagolovisotto.proteinaligner.alignment;

public class NoSimilarRegionException extends RuntimeException {

    public NoSimilarRegionException() {
        super("Las proteinas no comparten ninguna region con score positivo");
    }
}