package com.dro.modules.inventory.api.dto.response;

import com.dro.modules.inventory.domain.ItemDefinition;

public record ItemDefinitionResponse(
        Long id,
        String code,
        String name,
        String description,
        String category,
        boolean stackable,
        Integer buyPrice,
        Integer sellPrice,
        boolean tradable,
        boolean sellable,
        boolean usable,
        Integer maxStack,
        String rarity,
        String icon
) {
    public static ItemDefinitionResponse from(ItemDefinition item) {
        return new ItemDefinitionResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.isStackable(),
                item.getBuyPrice(),
                item.getSellPrice(),
                item.isTradable(),
                item.isSellable(),
                item.isUsable(),
                item.getMaxStack(),
                item.getRarity(),
                item.getIcon()
        );
    }
}