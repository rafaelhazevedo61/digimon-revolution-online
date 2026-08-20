package com.dro.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditResilienceTest {

    @Mock
    private AuditOutboxRepository outboxRepository;

    @Mock
    private TransactionAuditRepository transactionAuditRepository;

    @Test
    void markDeadLetter_preservesFailureAndStopsAutomaticProcessingState() {
        AuditOutboxEvent event = pendingEvent();
        event.markFailed("temporary failure", Instant.now());
        event.markDeadLetter("Mongo unavailable");

        assertThat(event.getStatus()).isEqualTo(AuditOutboxStatus.DEAD_LETTER);
        assertThat(event.getAttempts()).isEqualTo(2);
        assertThat(event.getLastError()).isEqualTo("Mongo unavailable");
    }

    @Test
    void processor_movesEventToDeadLetterAfterConfiguredAttempts() {
        AuditOutboxEvent event = pendingEvent();
        for (int attempt = 0; attempt < 5; attempt++) {
            event.markFailed("previous failure", Instant.now());
        }

        when(outboxRepository.findTop100ByStatusInAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
                anyList(), any(Instant.class)
        )).thenReturn(List.of(event));
        doThrow(new RuntimeException("Mongo unavailable"))
                .when(transactionAuditRepository)
                .findByEventId(event.getEventId());

        AuditOutboxProcessor processor = new AuditOutboxProcessor(
                outboxRepository,
                transactionAuditRepository,
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(processor, "maxAttempts", 5);

        processor.processAvailableEvents();

        assertThat(event.getStatus()).isEqualTo(AuditOutboxStatus.DEAD_LETTER);
        assertThat(event.getAttempts()).isEqualTo(6);
        verify(outboxRepository).save(event);
    }

    @Test
    void auditDocuments_defineExpectedRetentionIndexes() throws NoSuchFieldException {
        Indexed transactionIndex = indexedAnnotation(TransactionAuditDocument.class, "occurredAt");
        Indexed errorIndex = indexedAnnotation(ErrorLogDocument.class, "occurredAt");

        assertThat(transactionIndex.expireAfter()).isEqualTo("180d");
        assertThat(errorIndex.expireAfter()).isEqualTo("365d");
    }

    private static Indexed indexedAnnotation(Class<?> documentType, String fieldName) throws NoSuchFieldException {
        Field field = documentType.getDeclaredField(fieldName);
        Indexed indexed = field.getAnnotation(Indexed.class);
        assertThat(indexed).as("TTL index for %s.%s", documentType.getSimpleName(), fieldName).isNotNull();
        return indexed;
    }

    private static AuditOutboxEvent pendingEvent() {
        return AuditOutboxEvent.pending(
                "event-resilience-1",
                "SHOP_PURCHASE_COMPLETED",
                "ShopProduct",
                "product-1",
                "correlation-1",
                "{\"module\":\"shop\",\"operation\":\"SHOP_PURCHASE_COMPLETED\",\"summary\":\"test\"}"
        );
    }
}
