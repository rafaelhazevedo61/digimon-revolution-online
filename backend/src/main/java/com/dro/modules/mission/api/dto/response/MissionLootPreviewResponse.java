package com.dro.modules.mission.api.dto.response;

import java.util.List;

/**
 * Conteúdo que pode ser exibido ao jogador antes de iniciar uma missão.
 *
 * <p>O contrato diferencia recompensas fixas, loot legado sorteado diretamente
 * pela missão e o baú entregue pelas missões migradas para loot tables.</p>
 */
public record MissionLootPreviewResponse(
        String missionId,
        String missionName,
        int xpReward,
        int bitsReward,
        List<FixedReward> fixedRewards,
        ChestPreview chest,
        List<LootChance> lootChances,
        List<LegacyLootItem> lootItems
) {

    public record FixedReward(
            String itemType,
            String itemCode,
            String itemName,
            int quantity
    ) {
    }

    public record LootChance(
            String rarity,
            int chance
    ) {
    }

    public record LegacyLootItem(
            String rarity,
            String itemType,
            String itemCode,
            String itemName,
            int quantity
    ) {
    }

    public record ChestPreview(
            String code,
            String name,
            String description,
            String icon,
            int minItems,
            int maxItems,
            List<RarityWeight> rarityWeights,
            List<ChestLootItem> items
    ) {
    }

    public record RarityWeight(
            String rarity,
            int weight
    ) {
    }

    public record ChestLootItem(
            String rarity,
            String itemType,
            String itemCode,
            String itemName,
            int weight,
            int minQuantity,
            int maxQuantity,
            String equipmentTemplateName,
            String equipmentRarity
    ) {
    }
}
