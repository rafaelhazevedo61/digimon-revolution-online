package com.dro.modules.loot.api.dto.response;

import com.dro.modules.inventory.domain.ItemDefinition;

/**
 * Item catalogado disponibilizado para seleção na configuração administrativa.
 */
public record AdminLootItemCatalogResponse(
        Long id,
        String code,
        String name,
        String category,
        String rarity,
        boolean stackable,
        Integer maxStack,
        boolean tradable
) {

    public static AdminLootItemCatalogResponse from(ItemDefinition item) {
        return new AdminLootItemCatalogResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getCategory(),
                item.getRarity(),
                item.isStackable(),
                item.getMaxStack(),
                item.isTradable()
        );
    }
}
