package com.santiagolovisotto.proteinaligner.web;

class InvalidRequestException extends RuntimeException {

    InvalidRequestException(String message) {
        super(message);
    }
}