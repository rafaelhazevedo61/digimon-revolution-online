package com.dro.modules.inventory.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Inventário.
 */
public record GrantItemRequest(
        @NotNull UUID playerId,
        @NotBlank String itemCode,
        @Min(1) int quantity
) {
}
