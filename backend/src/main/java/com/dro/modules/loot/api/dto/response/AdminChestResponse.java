package com.dro.modules.loot.api.dto.response;

import com.dro.modules.loot.domain.ChestDefinitionEntity;

import java.time.LocalDateTime;

/**
 * Visão administrativa de um Baú da Área e de sua configuração de loot.
 */
public record AdminChestResponse(
        Long id,
        String code,
        String name,
        String description,
        String icon,
        boolean active,
        boolean tradable,
        String itemCode,
        String itemName,
        String lootTableCode,
        String lootTableName,
        boolean lootTableActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {

    /** Constrói a resposta dentro da transação que carregou o catálogo associado. */
    public static AdminChestResponse from(ChestDefinitionEntity chest) {
        return new AdminChestResponse(
                chest.getId(),
                chest.getCode(),
                chest.getName(),
                chest.getDescription(),
                chest.getIcon(),
                chest.isActive(),
                chest.isTradable(),
                chest.getItemDefinition() != null ? chest.getItemDefinition().getCode() : null,
                chest.getItemDefinition() != null ? chest.getItemDefinition().getName() : null,
                chest.getLootTable() != null ? chest.getLootTable().getCode() : null,
                chest.getLootTable() != null ? chest.getLootTable().getName() : null,
                chest.getLootTable() != null && chest.getLootTable().isActive(),
                chest.getCreatedAt(),
                chest.getUpdatedAt(),
                chest.getCreatedBy(),
                chest.getUpdatedBy()
        );
    }
}
