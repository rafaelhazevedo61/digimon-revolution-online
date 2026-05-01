package com.dro.modules.loot.domain;

import com.dro.shared.exception.BadRequestException;

import java.util.List;
import java.util.Random;

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
            throw new BadRequestException("No loot items configured for rarity: " + rarity);
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

        throw new BadRequestException("Invalid loot rarity chance configuration");
    }
}