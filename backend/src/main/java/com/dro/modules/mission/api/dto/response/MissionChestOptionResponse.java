package com.dro.modules.mission.api.dto.response;

import com.dro.modules.loot.domain.ChestDefinitionEntity;

/**
 * Baú temático disponível para vinculação a uma Missão.
 */
public record MissionChestOptionResponse(
        String code,
        String name,
        String description,
        String lootTableCode,
        String lootTableName,
        boolean active,
        boolean tradable
) {

    public static MissionChestOptionResponse from(ChestDefinitionEntity chest) {
        return new MissionChestOptionResponse(
                chest.getCode(),
                chest.getName(),
                chest.getDescription(),
                chest.getLootTable() != null ? chest.getLootTable().getCode() : null,
                chest.getLootTable() != null ? chest.getLootTable().getName() : null,
                chest.isActive(),
                chest.isTradable()
        );
    }
}
