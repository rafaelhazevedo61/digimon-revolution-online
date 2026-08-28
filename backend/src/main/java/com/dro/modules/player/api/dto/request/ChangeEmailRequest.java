package com.dro.modules.player.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados necessários para alterar o e-mail da própria conta.
 */
public record ChangeEmailRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Email
        String newEmail
) {
}
