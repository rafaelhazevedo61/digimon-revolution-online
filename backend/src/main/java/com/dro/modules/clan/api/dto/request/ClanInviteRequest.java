package com.dro.modules.clan.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanInviteRequest(
        @NotBlank(message = "Informe o nome do jogador.")
        @Size(max = 30, message = "O nome do jogador deve ter no máximo 30 caracteres.")
        String username
) {
}
