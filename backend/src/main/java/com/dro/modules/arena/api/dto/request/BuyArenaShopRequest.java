package com.dro.modules.arena.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrato de dados do módulo de Arena.
 */
public record BuyArenaShopRequest(
        @NotBlank String productCode,
        @Min(1) int quantity
) {}
