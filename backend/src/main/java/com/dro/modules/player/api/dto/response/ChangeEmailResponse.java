package com.dro.modules.player.api.dto.response;

/**
 * Resultado da alteração do e-mail da conta.
 */
public record ChangeEmailResponse(
        String token,
        String email
) {
}
