package com.dro.modules.inventory.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Inventário.
 */
public record UseItemRequest(
        @NotNull
        ItemType itemType,
        @Min(1)
        @Max(100)
        Integer quantity
) {}
