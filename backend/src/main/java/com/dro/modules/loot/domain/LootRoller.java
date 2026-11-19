package com.dro.modules.loot.domain;

import java.util.List;
import java.util.Random;

/**
 * Componente da camada de componente de domínio do módulo de Loot.
 */
public class LootRoller {

    private static final Random random = new Random();

    private LootRoller() {
    }

    public static LootItem roll(LootTable lootTable) {

        LootRarity rarity = rollRarity(lootTable.getRarityChances());

        List<LootItem> possibleItems = lootTable.getItems()
                .stream()
                .filter(item -> item.getRarity() == rarity)
                .toList();

        if (possibleItems.isEmpty()) {
            throw new RuntimeException("No loot items configured for rarity: " + rarity);
        }

        return possibleItems.get(random.nextInt(possibleItems.size()));
    }

    private static LootRarity rollRarity(List<LootRarityChance> chances) {

        int totalChance = chances.stream()
                .mapToInt(LootRarityChance::getChance)
                .sum();

        int roll = random.nextInt(totalChance);

        int accumulated = 0;

        for (LootRarityChance chance : chances) {
            accumulated += chance.getChance();

            if (roll < accumulated) {
                return chance.getRarity();
            }
        }

        throw new RuntimeException("Invalid loot rarity chance configuration");
    }
}