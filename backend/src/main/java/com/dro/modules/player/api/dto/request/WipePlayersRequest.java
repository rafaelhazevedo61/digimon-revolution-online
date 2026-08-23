package com.dro.modules.player.api.dto.request;

/**
 * Confirmação explícita para a operação destrutiva de limpeza de jogadores.
 *
 * @param confirmation deve ser exatamente {@code WIPE}; valores ausentes ou
 * diferentes são rejeitados com HTTP 400
 */
public record WipePlayersRequest(String confirmation) {
}
