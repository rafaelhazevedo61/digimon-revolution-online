package com.dro.modules.loot.domain;

import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Loot.
 */
public class LootTable {

    private final List<LootRarityChance> rarityChances;
    private final List<LootItem> items;

    public LootTable(List<LootRarityChance> rarityChances, List<LootItem> items) {
        this.rarityChances = rarityChances;
        this.items = items;
    }

    public List<LootRarityChance> getRarityChances() {
        return rarityChances;
    }

    public List<LootItem> getItems() {
        return items;
    }
}