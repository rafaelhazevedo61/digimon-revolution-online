package com.dro.shared.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableException extends BusinessException {

    public UnprocessableException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
