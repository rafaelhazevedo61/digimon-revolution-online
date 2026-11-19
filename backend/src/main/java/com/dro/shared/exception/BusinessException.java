package com.dro.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final ApiErrorCode code;

    public BusinessException(String message, HttpStatus status) {
        this(message, status, defaultCode(status));
    }

    public BusinessException(String message, HttpStatus status, ApiErrorCode code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    private static ApiErrorCode defaultCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ApiErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ApiErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ApiErrorCode.FORBIDDEN;
            case NOT_FOUND -> ApiErrorCode.NOT_FOUND;
            case CONFLICT -> ApiErrorCode.CONFLICT;
            case UNPROCESSABLE_ENTITY -> ApiErrorCode.UNPROCESSABLE_ENTITY;
            default -> ApiErrorCode.INTERNAL_ERROR;
        };
    }
}