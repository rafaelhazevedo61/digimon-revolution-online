package com.dro.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementação MongoDB da porta de armazenamento de auditoria.
 */
@Service
@RequiredArgsConstructor
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
}
