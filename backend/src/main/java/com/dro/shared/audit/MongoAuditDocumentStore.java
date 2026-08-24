package com.dro.shared.audit;

import org.springframework.stereotype.Service;

/**
 * Implementação MongoDB da porta de armazenamento de auditoria.
 */
@Service
public class MongoAuditDocumentStore implements AuditDocumentStore {
    private final TransactionAuditRepository transactionAuditRepository;
    private final ErrorLogRepository errorLogRepository;

    @Override
    public TransactionAuditDocument saveTransaction(TransactionAuditDocument document) {
        return transactionAuditRepository.save(document);
    }

    @Override
    public ErrorLogDocument saveError(ErrorLogDocument document) {
        return errorLogRepository.save(document);
    }

    public MongoAuditDocumentStore(final TransactionAuditRepository transactionAuditRepository, final ErrorLogRepository errorLogRepository) {
        this.transactionAuditRepository = transactionAuditRepository;
        this.errorLogRepository = errorLogRepository;
    }
}
