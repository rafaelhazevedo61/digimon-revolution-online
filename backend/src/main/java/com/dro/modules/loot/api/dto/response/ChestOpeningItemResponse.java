package com.dro.modules.loot.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;

/**
 * Item recebido em uma abertura de baú.
 */
public record ChestOpeningItemResponse(
        String itemCode,
        String itemName,
        LootRarity rarity,
        ItemType itemType,
        String materialCode,
        int quantity
) {
}
