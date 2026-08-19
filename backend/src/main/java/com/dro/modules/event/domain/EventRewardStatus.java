package com.dro.modules.event.domain;

/**
 * Estados possíveis do ciclo de vida de uma premiação de evento.
 */
public enum EventRewardStatus {
    /** Criada e disponível enquanto não tiver expirado. */
    PENDING,
    /** Entregue com sucesso ao Digimon ativo do destinatário. */
    CLAIMED,
    /** Deixou de estar disponível porque o prazo terminou. */
    EXPIRED,
    /** Cancelada antes do resgate e permanentemente indisponível. */
    CANCELLED
}
