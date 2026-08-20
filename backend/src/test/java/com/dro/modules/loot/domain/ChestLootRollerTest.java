package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.shared.exception.UnprocessableException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChestLootRollerTest {

    @Test
    void rollReturnsDistinctEntriesWithinConfiguredQuantityRanges() {
        LootTableEntity table = tableWithThreeEntriesPerRarity(2, 2);

        ChestLootRoller.ChestLootRoll result = new ChestLootRoller(new Random(7)).roll(table);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items())
                .extracting(ChestLootRoller.ChestLootItem::materialCode)
                .doesNotHaveDuplicates();
        assertThat(result.items())
                .allSatisfy(item -> assertThat(item.quantity()).isBetween(1, 3));
    }

    @Test
    void rollSupportsTheConfiguredRangeFromOneToFourItems() {
        LootTableEntity table = tableWithThreeEntriesPerRarity(1, 4);

        ChestLootRoller.ChestLootRoll result = new ChestLootRoller(new Random(12)).roll(table);

        assertThat(result.items().size()).isBetween(1, 4);
    }

    @Test
    void rollRejectsAConfigurationAboveFourItems() {
        LootTableEntity table = tableWithThreeEntriesPerRarity(1, 5);

        assertThatThrownBy(() -> new ChestLootRoller(new Random(1)).roll(table))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("configuration");
    }

    @Test
    void rollRejectsARewardRarityWithoutEnoughActiveEntries() {
        LootTableEntity table = baseTable(4, 4);
        table.getRarityWeights().forEach(weight -> weight.setWeight(
                weight.getRarity() == LootRarity.COMMON ? 1 : 0
        ));
        table.getEntries().add(entry(LootRarity.COMMON, "TRAINING_STONE", null, 1, 1, 1));

        assertThatThrownBy(() -> new ChestLootRoller(new Random(3)).roll(table))
                .isInstanceOf(UnprocessableException.class);
    }

    private LootTableEntity tableWithThreeEntriesPerRarity(int minItems, int maxItems) {
        LootTableEntity table = baseTable(minItems, maxItems);
        for (LootRarity rarity : LootRarity.values()) {
            table.getEntries().add(entry(rarity, "EVOLUTION_MATERIAL", rarity.name() + "_A", 10, 1, 3));
            table.getEntries().add(entry(rarity, "EVOLUTION_MATERIAL", rarity.name() + "_B", 5, 1, 3));
            table.getEntries().add(entry(rarity, "EVOLUTION_MATERIAL", rarity.name() + "_C", 1, 1, 3));
        }
        return table;
    }

    private LootTableEntity baseTable(int minItems, int maxItems) {
        LootTableEntity table = LootTableEntity.builder()
                .active(true)
                .minItems(minItems)
                .maxItems(maxItems)
                .build();
        List<LootTableRarityWeightEntity> weights = new ArrayList<>();
        for (LootRarity rarity : LootRarity.values()) {
            weights.add(LootTableRarityWeightEntity.builder()
                    .lootTable(table)
                    .rarity(rarity)
                    .weight(1)
                    .build());
        }
        table.setRarityWeights(weights);
        return table;
    }

    private LootTableEntryEntity entry(
            LootRarity rarity,
            String itemType,
            String materialCode,
            int weight,
            int minQuantity,
            int maxQuantity
    ) {
        return LootTableEntryEntity.builder()
                .rarity(rarity)
                .itemType(ItemType.valueOf(itemType))
                .materialCode(materialCode)
                .weight(weight)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .active(true)
                .build();
    }
}
