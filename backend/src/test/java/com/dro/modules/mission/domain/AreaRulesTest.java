package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaRulesTest {

    @Test
    void requiredStage_digitalForest_returnsBaby() {
        assertEquals(Stage.BABY, AreaRules.requiredStage(Area.DIGITAL_FOREST));
    }

    @Test
    void requiredStage_volcanicZone_returnsRookie() {
        assertEquals(Stage.ROOKIE, AreaRules.requiredStage(Area.VOLCANIC_ZONE));
    }

    @Test
    void requiredStage_oceanDepths_returnsChampion() {
        assertEquals(Stage.CHAMPION, AreaRules.requiredStage(Area.OCEAN_DEPTHS));
    }

    @Test
    void requiredStage_ancientRuins_returnsUltimate() {
        assertEquals(Stage.ULTIMATE, AreaRules.requiredStage(Area.ANCIENT_RUINS));
    }

    @Test
    void isUnlocked_babyStage_unlocksDigitalForest() {
        assertTrue(AreaRules.isUnlocked(Stage.BABY, Area.DIGITAL_FOREST));
    }

    @Test
    void isUnlocked_babyStage_doesNotUnlockVolcanicZone() {
        assertFalse(AreaRules.isUnlocked(Stage.BABY, Area.VOLCANIC_ZONE));
    }

    @Test
    void isUnlocked_megaStage_unlocksAll() {
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.DIGITAL_FOREST));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.VOLCANIC_ZONE));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.OCEAN_DEPTHS));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.ANCIENT_RUINS));
    }

    @Test
    void isUnlocked_championStage_unlocksUpToOceanDepths() {
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.DIGITAL_FOREST));
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.VOLCANIC_ZONE));
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.OCEAN_DEPTHS));
        assertFalse(AreaRules.isUnlocked(Stage.CHAMPION, Area.ANCIENT_RUINS));
    }
}
