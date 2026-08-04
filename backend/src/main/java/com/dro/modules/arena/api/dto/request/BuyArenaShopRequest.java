package com.dro.modules.arena.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BuyArenaShopRequest(
        @NotBlank String productCode,
        @Min(1) int quantity
) {}
