package com.dro.shared.audit;

/**
 * Porta de armazenamento dos documentos de auditoria persistente.
 */
public interface AuditDocumentStore {

    /** Persiste uma transação positiva de forma idempotente. */
    TransactionAuditDocument saveTransaction(TransactionAuditDocument document);

    /** Persiste um erro sanitizado de forma idempotente. */
    ErrorLogDocument saveError(ErrorLogDocument document);
}
