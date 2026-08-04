package com.dro.modules.arena.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

public record ArenaShopProductResponse(
        String code,
        String name,
        ItemType itemType,
        int quantity,
        int priceCoins
) {}
