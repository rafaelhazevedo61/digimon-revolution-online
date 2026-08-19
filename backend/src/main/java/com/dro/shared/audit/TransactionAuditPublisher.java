package com.dro.shared.audit;

import com.dro.shared.observability.CorrelationIdContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Fachada para publicar operações de negócio concluídas no Transactional Outbox.
 */
@Service
@RequiredArgsConstructor
public class TransactionAuditPublisher {

    private final AuditOutboxService outboxService;

    /** Enfileira uma auditoria positiva com o correlation ID da requisição atual. */
    public void success(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            Map<String, Object> payload
    ) {
        outboxService.enqueue(
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                CorrelationIdContext.current(),
                payload
        );
    }
}
