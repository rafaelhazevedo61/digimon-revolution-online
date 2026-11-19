package com.dro.modules.loot.api.dto.response;

import java.util.List;

public record ChestLootPreviewResponse(
        String code,
        String name,
        String description,
        String icon,
        int minItems,
        int maxItems,
        List<RarityWeight> rarityWeights,
        List<LootItem> items
) {
    public record RarityWeight(String rarity, int weight) {}

    public record LootItem(
            String rarity,
            String itemType,
            String itemCode,
            String itemName,
            int weight,
            int minQuantity,
            int maxQuantity,
            String equipmentTemplateName,
            String equipmentRarity
    ) {}
}
