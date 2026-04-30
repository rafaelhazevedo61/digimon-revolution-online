package com.dro.modules.loot.domain;

public class LootRarityChance {

    private final LootRarity rarity;
    private final int chance;

    public LootRarityChance(LootRarity rarity, int chance) {
        this.rarity = rarity;
        this.chance = chance;
    }

    public LootRarity getRarity() {
        return rarity;
    }

    public int getChance() {
        return chance;
    }
}