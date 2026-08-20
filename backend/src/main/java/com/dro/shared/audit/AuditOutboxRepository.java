package com.dro.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consultas dos eventos que aguardam publicação no MongoDB.
 */
public interface AuditOutboxRepository extends JpaRepository<AuditOutboxEvent, UUID> {

    /** Localiza um evento pelo identificador idempotente. */
    Optional<AuditOutboxEvent> findByEventId(String eventId);

    /** Retorna eventos pendentes ou falhos cuja próxima tentativa já está disponível. */
    List<AuditOutboxEvent> findTop100ByStatusInAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
            List<AuditOutboxStatus> statuses,
            Instant availableAt
    );
}
