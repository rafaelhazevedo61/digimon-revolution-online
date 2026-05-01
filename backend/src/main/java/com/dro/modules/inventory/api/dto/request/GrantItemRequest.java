package com.dro.modules.inventory.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantItemRequest(
        @NotNull UUID digimonId,
        @NotNull ItemType itemType,
        @Min(1) int quantity
) {
}
