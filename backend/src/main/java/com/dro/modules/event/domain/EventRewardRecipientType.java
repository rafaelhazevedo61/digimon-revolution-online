package com.dro.modules.event.domain;

/**
 * Estratégia usada para expandir os destinatários de uma premiação.
 */
public enum EventRewardRecipientType {
    /** Um único jogador identificado pelo username. */
    PLAYER,
    /** Todos os jogadores que pertencem ao clã selecionado no momento do envio. */
    CLAN,
    /** Lista manual de jogadores, limitada pelo caso de uso a 100 destinatários. */
    PLAYERS,
    /** Todos os jogadores com conta do tipo PLAYER no momento do envio. */
    ALL_PLAYERS
}
