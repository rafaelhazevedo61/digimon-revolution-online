package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvolutionRulesTest {

    @Test
    void nextStage_baby_returnsRookie() {
        assertEquals(Stage.ROOKIE, EvolutionRules.nextStage(Stage.BABY));
    }

    @Test
    void nextStage_rookie_returnsChampion() {
        assertEquals(Stage.CHAMPION, EvolutionRules.nextStage(Stage.ROOKIE));
    }

    @Test
    void nextStage_champion_returnsUltimate() {
        assertEquals(Stage.ULTIMATE, EvolutionRules.nextStage(Stage.CHAMPION));
    }

    @Test
    void nextStage_ultimate_returnsMega() {
        assertEquals(Stage.MEGA, EvolutionRules.nextStage(Stage.ULTIMATE));
    }

    @Test
    void nextStage_mega_returnsNull() {
        assertNull(EvolutionRules.nextStage(Stage.MEGA));
    }

    @Test
    void requiredLevel_baby_returns10() {
        assertEquals(10, EvolutionRules.requiredLevel(Stage.BABY));
    }

    @Test
    void requiredLevel_rookie_returns25() {
        assertEquals(25, EvolutionRules.requiredLevel(Stage.ROOKIE));
    }

    @Test
    void requiredLevel_champion_returns50() {
        assertEquals(50, EvolutionRules.requiredLevel(Stage.CHAMPION));
    }

    @Test
    void requiredLevel_ultimate_returns75() {
        assertEquals(75, EvolutionRules.requiredLevel(Stage.ULTIMATE));
    }

    @Test
    void requiredLevel_mega_returnsMaxValue() {
        assertEquals(Integer.MAX_VALUE, EvolutionRules.requiredLevel(Stage.MEGA));
    }

    @Test
    void requiredFragment_rookie_returnsFragmentChampion() {
        assertEquals(ItemType.FRAGMENT_CHAMPION, EvolutionRules.requiredFragment(Stage.ROOKIE));
    }

    @Test
    void requiredFragment_champion_returnsFragmentUltimate() {
        assertEquals(ItemType.FRAGMENT_ULTIMATE, EvolutionRules.requiredFragment(Stage.CHAMPION));
    }

    @Test
    void requiredFragment_ultimate_returnsFragmentMega() {
        assertEquals(ItemType.FRAGMENT_MEGA, EvolutionRules.requiredFragment(Stage.ULTIMATE));
    }

    @Test
    void requiredFragment_baby_returnsNull() {
        assertNull(EvolutionRules.requiredFragment(Stage.BABY));
    }

    @Test
    void requiredFragmentQuantity_rookie_returns5() {
        assertEquals(5, EvolutionRules.requiredFragmentQuantity(Stage.ROOKIE));
    }

    @Test
    void requiredFragmentQuantity_champion_returns10() {
        assertEquals(10, EvolutionRules.requiredFragmentQuantity(Stage.CHAMPION));
    }

    @Test
    void requiredFragmentQuantity_ultimate_returns20() {
        assertEquals(20, EvolutionRules.requiredFragmentQuantity(Stage.ULTIMATE));
    }

    @Test
    void stageStatMultiplier_followsProgression() {
        assertEquals(1.0, EvolutionRules.stageStatMultiplier(Stage.BABY));
        assertEquals(1.2, EvolutionRules.stageStatMultiplier(Stage.ROOKIE));
        assertEquals(1.5, EvolutionRules.stageStatMultiplier(Stage.CHAMPION));
        assertEquals(2.0, EvolutionRules.stageStatMultiplier(Stage.ULTIMATE));
        assertEquals(2.8, EvolutionRules.stageStatMultiplier(Stage.MEGA));
    }
}
