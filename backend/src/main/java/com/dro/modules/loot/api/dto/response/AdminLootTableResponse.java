package com.dro.modules.loot.api.dto.response;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootTableEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Visão administrativa completa de uma Loot Table nomeada.
 */
public record AdminLootTableResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        int minItems,
        int maxItems,
        List<RarityWeightResponse> rarityWeights,
        List<EntryResponse> entries,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {

    /** Peso persistido de raridade. */
    public record RarityWeightResponse(LootRarity rarity, int weight) {
    }

    /** Entrada persistida e identificada pelo catálogo. */
    public record EntryResponse(
            Long id,
            LootRarity rarity,
            ItemType itemType,
            String materialCode,
            int weight,
            int minQuantity,
            int maxQuantity,
            boolean active,
            String itemCode,
            String itemName,
            String itemCategory,
            String itemRarity
    ) {
    }

    /** Constrói a resposta mantendo a coleção já carregada pela consulta admin. */
    public static AdminLootTableResponse from(
            LootTableEntity entity,
            Map<String, ItemDefinition> catalog
    ) {
        List<RarityWeightResponse> weights = entity.getRarityWeights().stream()
                .map(weight -> new RarityWeightResponse(weight.getRarity(), weight.getWeight()))
                .toList();

        List<EntryResponse> entries = entity.getEntries().stream()
                .map(entry -> {
                    String catalogCode = entry.getMaterialCode() != null
                            ? entry.getMaterialCode()
                            : entry.getItemType().name();
                    ItemDefinition definition = catalog.get(catalogCode);
                    return new EntryResponse(
                            entry.getId(),
                            entry.getRarity(),
                            entry.getItemType(),
                            entry.getMaterialCode(),
                            entry.getWeight(),
                            entry.getMinQuantity(),
                            entry.getMaxQuantity(),
                            entry.isActive(),
                            catalogCode,
                            definition != null ? definition.getName() : catalogCode,
                            definition != null ? definition.getCategory() : null,
                            definition != null ? definition.getRarity() : null
                    );
                })
                .toList();

        return new AdminLootTableResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getMinItems(),
                entity.getMaxItems(),
                weights,
                entries,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
}
