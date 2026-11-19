package com.dro.modules.player.api.dto.response;

/** Resultado da alteração do username da conta. */
public record ChangeUsernameResponse(
        String token,
        String username,
        int cost,
        int remainingBits,
        int usernameChangeCount
) {
}
