package com.dro.shared.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Persistência dos eventos positivos de auditoria.
 */
public interface TransactionAuditRepository extends MongoRepository<TransactionAuditDocument, String> {

    /** Localiza um evento pelo identificador idempotente. */
    Optional<TransactionAuditDocument> findByEventId(String eventId);
}
