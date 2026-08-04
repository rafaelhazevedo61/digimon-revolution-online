package com.dro.modules.arena.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

public record BuyArenaShopResponse(
        String productCode,
        String productName,
        ItemType itemType,
        int quantity,
        int totalPrice,
        int arenaCoinsBalance,
        String message
) {}
