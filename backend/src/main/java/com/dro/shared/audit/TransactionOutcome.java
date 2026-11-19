package com.dro.shared.audit;

/**
 * Resultado da operação transacional associada a um registro de erro.
 */
public enum TransactionOutcome {
    ROLLED_BACK,
    REJECTED,
    UNKNOWN,
    NOT_APPLICABLE
}
