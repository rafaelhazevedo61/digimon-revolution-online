package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;

public class LootItem {

    private final LootRarity rarity;
    private final ItemType itemType;
    private final int quantity;

    public LootItem(LootRarity rarity, ItemType itemType, int quantity) {
        this.rarity = rarity;
        this.itemType = itemType;
        this.quantity = quantity;
    }

    public LootRarity getRarity() {
        return rarity;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getQuantity() {
        return quantity;
    }
}