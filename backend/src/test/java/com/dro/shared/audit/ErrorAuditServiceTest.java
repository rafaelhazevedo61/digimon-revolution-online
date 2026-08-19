package com.dro.shared.audit;

import com.dro.shared.exception.ApiErrorCode;
import com.dro.shared.observability.CorrelationIdContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorAuditServiceTest {

    @Mock
    private AuditDocumentStore documentStore;

    @Mock
    private HttpServletRequest request;

    @AfterEach
    void clearContext() {
        CorrelationIdContext.clear();
    }

    @Test
    void record_persistsSanitizedErrorWithCorrelation() {
        CorrelationIdContext.put("request-1");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/mail");
        ErrorAuditService service = new ErrorAuditService(documentStore);

        service.record(
                request,
                new IllegalArgumentException("password=secret\u0000"),
                ApiErrorCode.BAD_REQUEST,
                400,
                TransactionOutcome.REJECTED
        );

        ArgumentCaptor<ErrorLogDocument> captor = ArgumentCaptor.forClass(ErrorLogDocument.class);
        verify(documentStore).saveError(captor.capture());
        ErrorLogDocument document = captor.getValue();
        assertThat(document.correlationId()).isEqualTo("request-1");
        assertThat(document.path()).isEqualTo("/mail");
        assertThat(document.transactionOutcome()).isEqualTo(TransactionOutcome.REJECTED);
        assertThat(document.message()).doesNotContain("\u0000");
        assertThat(document.severity()).isEqualTo(AuditSeverity.WARN);
    }

    @Test
    void record_doesNotPropagateMongoFailure() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/health");
        doThrow(new RuntimeException("Mongo unavailable"))
                .when(documentStore)
                .saveError(org.mockito.ArgumentMatchers.any());
        ErrorAuditService service = new ErrorAuditService(documentStore);

        service.record(
                request,
                new RuntimeException("failure"),
                ApiErrorCode.INTERNAL_ERROR,
                500,
                TransactionOutcome.UNKNOWN
        );

        verify(documentStore).saveError(org.mockito.ArgumentMatchers.any());
    }
}
