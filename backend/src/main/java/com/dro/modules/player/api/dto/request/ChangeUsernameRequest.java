package com.dro.modules.player.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados necessários para alterar o username da própria conta. */
public record ChangeUsernameRequest(
        @NotBlank(message = "O novo username é obrigatório.")
        @Size(min = 3, max = 50, message = "O username deve ter entre 3 e 50 caracteres.")
        String newUsername
) {
}
