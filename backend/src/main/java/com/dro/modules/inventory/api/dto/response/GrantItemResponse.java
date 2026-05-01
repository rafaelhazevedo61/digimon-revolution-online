package com.dro.modules.inventory.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

import java.util.UUID;

public record GrantItemResponse(
        UUID digimonId,
        ItemType itemType,
        int quantity,
        String message
) {
}
