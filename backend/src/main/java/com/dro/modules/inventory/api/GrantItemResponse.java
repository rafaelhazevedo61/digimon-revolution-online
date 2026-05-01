package com.dro.modules.inventory.api;

import com.dro.modules.inventory.domain.ItemType;

import java.util.UUID;

public record GrantItemResponse(
        UUID playerId,
        ItemType itemType,
        int quantity,
        String message
) {
}
