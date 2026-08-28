package com.dro.modules.clan.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanCreateRequest(
        @NotBlank @Size(max = 30) String name,
        @NotBlank @Size(min = 2, max = 3) String tag,
        @Size(max = 280) String description
) {
}
