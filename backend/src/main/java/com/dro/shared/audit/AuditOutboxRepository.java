package com.dro.shared.audit;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT COUNT(e) FROM AuditOutboxEvent e WHERE e.status IN (com.dro.shared.audit.AuditOutboxStatus.PUBLISHED, com.dro.shared.audit.AuditOutboxStatus.DEAD_LETTER) AND COALESCE(e.publishedAt, e.createdAt) < :cutoff")
    long countCompletedBefore(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query(value = "DELETE FROM audit_outbox_events WHERE id IN (SELECT id FROM audit_outbox_events WHERE status IN ('PUBLISHED', 'DEAD_LETTER') AND COALESCE(published_at, created_at) < :cutoff ORDER BY COALESCE(published_at, created_at) ASC LIMIT :batchSize)", nativeQuery = true)
    int deleteCompletedBefore(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
