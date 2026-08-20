package com.dro.modules.loot.api.dto.response;

import com.dro.modules.loot.domain.LootRarity;

import java.util.List;

/**
 * Resultado público de uma abertura de baú.
 */
public record ChestOpeningResponse(
        String requestId,
        String chestCode,
        String chestName,
        LootRarity rarity,
        List<ChestOpeningItemResponse> items,
        String message
) {
}
