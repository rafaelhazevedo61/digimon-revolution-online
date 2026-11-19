package com.dro.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de auditoria persistido na mesma transação do estado oficial.
 *
 * <p>O processador publica eventos {@link AuditOutboxStatus#PENDING} no MongoDB
 * e mantém o registro até a publicação ser confirmada.</p>
 */
@Entity
@Table(name = "audit_outbox_events")
public class AuditOutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String eventId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false, length = 80)
    private String aggregateId;

    @Column(length = 64)
    private String correlationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditOutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant availableAt;

    private Instant publishedAt;

    @Column(length = 1_000)
    private String lastError;

    @Version
    private long version;

    protected AuditOutboxEvent() {
    }

    private AuditOutboxEvent(
            UUID id,
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String correlationId,
            String payloadJson,
            Instant createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.payloadJson = payloadJson;
        this.status = AuditOutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = createdAt;
        this.availableAt = createdAt;
    }

    /** Cria evento pendente pronto para ser persistido junto à operação de negócio. */
    public static AuditOutboxEvent pending(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String correlationId,
            String payloadJson
    ) {
        return new AuditOutboxEvent(
                UUID.randomUUID(),
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                correlationId,
                payloadJson,
                Instant.now()
        );
    }

    /** Marca publicação confirmada no MongoDB. */
    public void markPublished(Instant publishedAt) {
        this.status = AuditOutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    /** Registra falha e agenda nova tentativa sem perder o evento. */
    public void markFailed(String error, Instant nextAttemptAt) {
        this.status = AuditOutboxStatus.FAILED;
        this.attempts++;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1_000));
        this.availableAt = nextAttemptAt;
    }

    /** Marca falha definitiva para interromper retries automáticos infinitos. */
    public void markDeadLetter(String error) {
        this.status = AuditOutboxStatus.DEAD_LETTER;
        this.attempts++;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1_000));
    }

    public UUID getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public AuditOutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAvailableAt() {
        return availableAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getLastError() {
        return lastError;
    }
}
