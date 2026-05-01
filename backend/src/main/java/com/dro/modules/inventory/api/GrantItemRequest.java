package com.dro.modules.inventory.api;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantItemRequest(
        @NotNull UUID playerId,
        @NotNull ItemType itemType,
        @Min(1) int quantity
) {
}
