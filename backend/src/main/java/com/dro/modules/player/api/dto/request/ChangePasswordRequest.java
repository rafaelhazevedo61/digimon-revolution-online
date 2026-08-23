package com.dro.modules.player.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record ChangePasswordRequest(

        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(
                min = 8,
                max = 60,
                message = "A nova senha deve ter entre 8 e 60 caracteres."
        )
        String newPassword

) {}