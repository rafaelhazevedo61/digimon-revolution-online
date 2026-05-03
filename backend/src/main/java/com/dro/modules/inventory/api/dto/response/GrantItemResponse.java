package com.dro.modules.inventory.api.dto.response;

import java.util.UUID;

public record GrantItemResponse(
        UUID digimonId,
        String itemCode,
        int quantity,
        String message
) {
}
