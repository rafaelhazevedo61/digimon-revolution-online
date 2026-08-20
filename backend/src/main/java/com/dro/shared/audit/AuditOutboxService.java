package com.dro.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Enfileira auditoria positiva dentro da transação do PostgreSQL.
 */
@Service
@RequiredArgsConstructor
public class AuditOutboxService {

    private final AuditOutboxRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Cria um evento pendente de forma idempotente.
     *
     * <p>Quando chamado dentro de um caso de uso transacional, o evento só
     * permanece após o commit da mesma transação.</p>
     */
    @Transactional
    public AuditOutboxEvent enqueue(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String correlationId,
            Map<String, Object> payload
    ) {
        return repository.findByEventId(eventId)
                .orElseGet(() -> repository.save(AuditOutboxEvent.pending(
                        eventId,
                        eventType,
                        aggregateType,
                        aggregateId,
                        correlationId,
                        serialize(payload)
                )));
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize audit outbox payload", exception);
        }
    }
}
