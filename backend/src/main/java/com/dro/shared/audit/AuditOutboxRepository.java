package com.dro.shared.audit;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

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

    /**
     * Retorna e bloqueia eventos prontos para publicação durante o processamento.
     *
     * <p>O lock evita que duas instâncias da aplicação processem o mesmo evento
     * simultaneamente e disputem o campo {@code version} do JPA.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AuditOutboxEvent> findTop100ByStatusInAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
            List<AuditOutboxStatus> statuses,
            Instant availableAt
    );
}
