package com.dro.modules.loot.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Item recebido em uma abertura de baú.
 */
public record ChestOpeningItemResponse(
        String itemCode,
        String itemName,
        ItemType itemType,
        String materialCode,
        int quantity
) {
}
