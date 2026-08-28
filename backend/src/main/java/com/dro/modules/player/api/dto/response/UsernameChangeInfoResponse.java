package com.dro.modules.player.api.dto.response;

/** Informações usadas para o preview da próxima troca de username. */
public record UsernameChangeInfoResponse(
        String username,
        int cost,
        int availableBits,
        int usernameChangeCount
) {
}
