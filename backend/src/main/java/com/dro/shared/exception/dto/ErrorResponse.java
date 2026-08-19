package com.dro.shared.exception.dto;

import com.dro.shared.exception.ApiErrorCode;
import com.dro.shared.observability.CorrelationIdContext;

import java.util.List;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        ApiErrorCode code,
        String message,
        String path,
        List<FieldErrorResponse> fields,
        String correlationId
) {
    public static ErrorResponse of(
            int status,
            String error,
            ApiErrorCode code,
            String message,
            String path
    ) {
        return new ErrorResponse(
                java.time.OffsetDateTime.now().toString(),
                status,
                error,
                code,
                message,
                path,
                List.of(),
                CorrelationIdContext.current()
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
                java.time.OffsetDateTime.now().toString(),
                status,
                error,
                code,
                message,
                path,
                fields == null ? List.of() : fields,
                CorrelationIdContext.current()
        );
    }
}