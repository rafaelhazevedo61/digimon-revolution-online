package com.dro.modules.shop.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrato de dados do módulo de Loja.
 */
public record BuyShopProductRequest(
        @NotBlank String productCode,
        @Min(1) @Max(999) int quantity
) {
}