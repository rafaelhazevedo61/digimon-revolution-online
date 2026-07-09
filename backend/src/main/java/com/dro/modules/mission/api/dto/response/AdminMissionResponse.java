package com.dro.modules.mission.api.dto.response;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;

import java.time.LocalDateTime;
import java.util.List;

public record AdminMissionResponse(
        String id,
        String name,
        String description,
        Area area,
        Stage requiredStage,
        int requiredLevel,
        int baseXp,
        int baseBits,
        int energyCost,
        int durationSeconds,
        boolean active,
        List<RewardDto> rewards,
        List<LootChanceDto> lootChances,
        List<LootItemDto> lootItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {

    public record RewardDto(Long id, ItemType itemType, int baseQuantity) {}
    public record LootChanceDto(Long id, LootRarity rarity, int chance) {}
    public record LootItemDto(Long id, LootRarity rarity, ItemType itemType, int quantity) {}

    public static AdminMissionResponse from(MissionDefinitionEntity entity) {
        List<RewardDto> rewards = entity.getRewards().stream()
                .map(r -> new RewardDto(r.getId(), r.getItemType(), r.getBaseQuantity()))
                .toList();

        List<LootChanceDto> lootChances = entity.getLootChances().stream()
                .map(c -> new LootChanceDto(c.getId(), c.getRarity(), c.getChance()))
                .toList();

        List<LootItemDto> lootItems = entity.getLootItems().stream()
                .map(i -> new LootItemDto(i.getId(), i.getRarity(), i.getItemType(), i.getQuantity()))
                .toList();

        return new AdminMissionResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getArea(),
                entity.getRequiredStage(),
                entity.getRequiredLevel(),
                entity.getBaseXp(),
                entity.getBaseBits(),
                entity.getEnergyCost(),
                entity.getDurationSeconds(),
                entity.isActive(),
                rewards,
                lootChances,
                lootItems,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
}
