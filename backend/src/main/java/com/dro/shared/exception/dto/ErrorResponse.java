package com.dro.shared.exception.dto;

import com.dro.shared.exception.ApiErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        ApiErrorCode code,
        String message,
        String path,
        List<FieldErrorResponse> fields
) {
    public static ErrorResponse of(
            int status,
            String error,
            ApiErrorCode code,
            String message,
            String path
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                code,
                message,
                path,
                List.of()
        );
    }

    public static ErrorResponse of(
            int status,
            String error,
            ApiErrorCode code,
            String message,
            String path,
            List<FieldErrorResponse> fields
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                code,
                message,
                path,
                fields == null ? List.of() : fields
        );
    }
}