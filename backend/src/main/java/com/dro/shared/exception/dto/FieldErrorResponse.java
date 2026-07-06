package com.dro.shared.exception.dto;

public record FieldErrorResponse(
        String field,
        String message
) {
}