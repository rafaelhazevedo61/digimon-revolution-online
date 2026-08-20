package com.dro.shared.audit;

/**
 * Estado de publicação de um evento de auditoria no Transactional Outbox.
 */
public enum AuditOutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
