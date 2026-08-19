package com.dro.shared.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Publica eventos do Outbox no MongoDB após o commit da transação oficial.
 */
@Service
@RequiredArgsConstructor
public class AuditOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(AuditOutboxProcessor.class);

    private final AuditOutboxRepository outboxRepository;
    private final TransactionAuditRepository transactionAuditRepository;
    private final ObjectMapper objectMapper;

    @Value("${dro.audit.outbox.max-attempts:5}")
    private int maxAttempts;

    /** Executa um lote pequeno de eventos prontos para publicação. */
    @Scheduled(fixedDelayString = "${dro.audit.outbox.fixed-delay-ms:5000}")
    @Transactional
    public void processAvailableEvents() {
        List<AuditOutboxEvent> events = outboxRepository
                .findTop100ByStatusInAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
                        List.of(AuditOutboxStatus.PENDING, AuditOutboxStatus.FAILED),
                        Instant.now()
                );
        events.forEach(this::processOne);
    }

    private void processOne(AuditOutboxEvent event) {
        try {
            if (transactionAuditRepository.findByEventId(event.getEventId()).isEmpty()) {
                transactionAuditRepository.save(toTransactionDocument(event));
            }
            event.markPublished(Instant.now());
            outboxRepository.save(event);
        } catch (RuntimeException exception) {
            if (event.getAttempts() >= maxAttempts) {
                event.markDeadLetter(exception.getMessage());
                log.error(
                        "Audit outbox event moved to dead letter. eventId={}, attempts={}",
                        event.getEventId(),
                        event.getAttempts(),
                        exception
                );
            } else {
                event.markFailed(exception.getMessage(), nextAttemptAt(event.getAttempts()));
                log.warn(
                        "Could not publish audit outbox event; retry scheduled. eventId={}, attempts={}, maxAttempts={}",
                        event.getEventId(),
                        event.getAttempts(),
                        maxAttempts,
                        exception
                );
            }
            outboxRepository.save(event);
        }
    }

    private TransactionAuditDocument toTransactionDocument(AuditOutboxEvent event) {
        Map<String, Object> payload = readPayload(event.getPayloadJson());
        return new TransactionAuditDocument(
                null,
                event.getEventId(),
                event.getCreatedAt(),
                event.getCorrelationId(),
                stringValue(payload, "actorId"),
                stringValue(payload, "module", event.getAggregateType()),
                stringValue(payload, "operation", event.getEventType()),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                AuditResult.SUCCESS,
                stringValue(payload, "summary", event.getEventType()),
                payload,
                numberValue(payload, "durationMs"),
                TransactionAuditDocument.CURRENT_SCHEMA_VERSION
        );
    }

    private Map<String, Object> readPayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Could not read audit outbox payload", exception);
        }
    }

    private static String stringValue(Map<String, Object> payload, String key) {
        return stringValue(payload, key, null);
    }

    private static String stringValue(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static Long numberValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private static Instant nextAttemptAt(int attempts) {
        long seconds = Math.min(300, 5L * (1L << Math.min(attempts, 6)));
        return Instant.now().plus(Duration.ofSeconds(seconds));
    }
}
