package com.dro.shared.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Persistência dos erros sanitizados da aplicação.
 */
public interface ErrorLogRepository extends MongoRepository<ErrorLogDocument, String> {

    /** Localiza um erro pelo identificador idempotente. */
    Optional<ErrorLogDocument> findByErrorId(String errorId);
}
