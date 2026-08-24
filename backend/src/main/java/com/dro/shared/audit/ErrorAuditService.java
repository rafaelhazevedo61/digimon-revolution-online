package com.dro.shared.audit;

import com.dro.shared.exception.ApiErrorCode;
import com.dro.shared.observability.CorrelationIdContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Registra erros HTTP em MongoDB sem expor segredos ou interromper a resposta ao cliente.
 */
@Service
public class ErrorAuditService {

    private static final Logger log = LoggerFactory.getLogger(ErrorAuditService.class);
    private static final int MAX_MESSAGE_LENGTH = 1_000;
    private static final int MAX_STACK_TRACE_LENGTH = 8_000;

    private final AuditDocumentStore documentStore;

    public ErrorAuditService(AuditDocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    /** Persiste um erro sanitizado; falha de auditoria fica apenas no log técnico. */
    public void record(
            HttpServletRequest request,
            Throwable exception,
            ApiErrorCode errorCode,
            int httpStatus,
            TransactionOutcome transactionOutcome
    ) {
        String correlationId = CorrelationIdContext.current();

        if (httpStatus >= 500) {
            log.error(
                    "Application error. correlationId={}, method={}, path={}, status={}, errorCode={}",
                    correlationId,
                    request.getMethod(),
                    request.getRequestURI(),
                    httpStatus,
                    errorCode,
                    exception
            );
        }

        ErrorLogDocument document = new ErrorLogDocument(
                null,
                UUID.randomUUID().toString(),
                Instant.now(),
                correlationId,
                null,
                "http",
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                httpStatus,
                errorCode.name(),
                exception.getClass().getName(),
                sanitize(exception.getMessage(), MAX_MESSAGE_LENGTH),
                sanitize(stackTrace(exception), MAX_STACK_TRACE_LENGTH),
                transactionOutcome,
                Map.of(),
                0,
                severityFor(httpStatus),
                ErrorLogDocument.CURRENT_SCHEMA_VERSION
        );

        try {
            documentStore.saveError(document);
        } catch (RuntimeException auditFailure) {
            log.error(
                    "Could not persist error audit. correlationId={}, originalException={}",
                    correlationId,
                    exception.getClass().getName(),
                    auditFailure
            );
        }
    }

    private static AuditSeverity severityFor(int status) {
        if (status >= 500) {
            return AuditSeverity.CRITICAL;
        }
        if (status >= 400) {
            return AuditSeverity.WARN;
        }
        return AuditSeverity.ERROR;
    }

    private static String stackTrace(Throwable exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static String sanitize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String sanitized = value.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "?");
        return sanitized.length() <= maxLength
                ? sanitized
                : sanitized.substring(0, maxLength);
    }
}
