package com.dro.modules.mission.domain;

import com.dro.modules.loot.domain.LootItem;
import com.dro.modules.loot.domain.LootRarityChance;
import com.dro.modules.loot.domain.LootTable;

import java.util.List;

public class MissionDefinitionMapper {

    private MissionDefinitionMapper() {
    }

    public static MissionDefinition toDefinition(MissionDefinitionEntity entity) {
        List<MissionReward> rewards = entity.getRewards().stream()
                .map(r -> new MissionReward(r.getItemType(), r.getBaseQuantity()))
                .toList();

        List<LootRarityChance> rarityChances = entity.getLootChances().stream()
                .map(c -> new LootRarityChance(c.getRarity(), c.getChance()))
                .toList();

        List<LootItem> lootItems = entity.getLootItems().stream()
                .map(i -> new LootItem(i.getRarity(), i.getItemType(), i.getQuantity()))
                .toList();

        LootTable lootTable = rarityChances.isEmpty() ? null : new LootTable(rarityChances, lootItems);

        return new MissionDefinition(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getArea(),
                entity.getRequiredStage(),
                entity.getRequiredLevel(),
                entity.getBaseXp(),
                entity.getEnergyCost(),
                entity.getDurationSeconds(),
                rewards,
                lootTable
        );
    }
}
