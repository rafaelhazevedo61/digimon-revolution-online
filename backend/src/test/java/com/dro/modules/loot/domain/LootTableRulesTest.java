package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LootTableRulesTest {

    @Test
    void acceptsAllOfficialPositiveRarityWeights() {
        assertThatCode(() -> LootTableRules.validateRarityWeights(Map.of(
                LootRarity.COMMON, 70,
                LootRarity.RARE, 20,
                LootRarity.EPIC, 8,
                LootRarity.LEGENDARY, 2
        ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingOfficialRarity() {
        assertThatThrownBy(() -> LootTableRules.validateRarityWeights(Map.of(
                LootRarity.COMMON, 70,
                LootRarity.RARE, 20,
                LootRarity.EPIC, 10
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All official rarities");
    }

    @Test
    void rejectsNonPositiveEntryWeight() {
        assertThatThrownBy(() -> LootTableRules.validateEntry(
                ItemType.TRAINING_STONE, null, 0, 1, 1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weight");
    }

    @Test
    void rejectsInvertedQuantityRange() {
        assertThatThrownBy(() -> LootTableRules.validateEntry(
                ItemType.DATA_CORE, null, 10, 4, 2
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity range");
    }

    @Test
    void requiresMaterialCodeForEvolutionMaterial() {
        assertThatThrownBy(() -> LootTableRules.validateEntry(
                ItemType.EVOLUTION_MATERIAL, null, 10, 1, 5
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material code");
    }

    @Test
    void requiresChestCodeForLootChest() {
        assertThatThrownBy(() -> LootTableRules.validateEntry(
                ItemType.LOOT_CHEST, "  ", 10, 1, 1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Material code");
    }

    @Test
    void acceptsNamedEvolutionMaterialWithValidRange() {
        assertThatCode(() -> LootTableRules.validateEntry(
                ItemType.EVOLUTION_MATERIAL, "FRAGMENT_AGUMON", 35, 1, 5
        )).doesNotThrowAnyException();
    }
}
