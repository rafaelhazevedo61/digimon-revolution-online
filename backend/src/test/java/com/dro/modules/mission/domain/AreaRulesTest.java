package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaRulesTest {

    @Test
    void requiredStage_nativeForest_returnsBaby() {
        assertEquals(Stage.BABY, AreaRules.requiredStage(Area.NATIVE_FOREST));
    }

    @Test
    void requiredStage_gearSavanna_returnsBabyII() {
        assertEquals(Stage.BABY_II, AreaRules.requiredStage(Area.GEAR_SAVANNA));
    }

    @Test
    void requiredStage_factorialTown_returnsRookie() {
        assertEquals(Stage.ROOKIE, AreaRules.requiredStage(Area.FACTORIAL_TOWN));
    }

    @Test
    void requiredStage_freezeland_returnsChampion() {
        assertEquals(Stage.CHAMPION, AreaRules.requiredStage(Area.FREEZELAND));
    }

    @Test
    void requiredStage_serverDesert_returnsUltimate() {
        assertEquals(Stage.ULTIMATE, AreaRules.requiredStage(Area.SERVER_DESERT));
    }

    @Test
    void requiredStage_infinityMountain_returnsMega() {
        assertEquals(Stage.MEGA, AreaRules.requiredStage(Area.INFINITY_MOUNTAIN));
    }

    @Test
    void isUnlocked_babyStage_unlocksNativeForest() {
        assertTrue(AreaRules.isUnlocked(Stage.BABY, Area.NATIVE_FOREST));
    }

    @Test
    void isUnlocked_babyStage_doesNotUnlockGearSavanna() {
        assertFalse(AreaRules.isUnlocked(Stage.BABY, Area.GEAR_SAVANNA));
    }

    @Test
    void isUnlocked_megaStage_unlocksAll() {
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.NATIVE_FOREST));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.GEAR_SAVANNA));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.FACTORIAL_TOWN));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.FREEZELAND));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.SERVER_DESERT));
        assertTrue(AreaRules.isUnlocked(Stage.MEGA, Area.INFINITY_MOUNTAIN));
    }

    @Test
    void isUnlocked_championStage_unlocksUpToFreezeland() {
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.NATIVE_FOREST));
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.GEAR_SAVANNA));
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.FACTORIAL_TOWN));
        assertTrue(AreaRules.isUnlocked(Stage.CHAMPION, Area.FREEZELAND));
        assertFalse(AreaRules.isUnlocked(Stage.CHAMPION, Area.SERVER_DESERT));
        assertFalse(AreaRules.isUnlocked(Stage.CHAMPION, Area.INFINITY_MOUNTAIN));
    }

    @Test
    void isUnlocked_ultimateStage_doesNotUnlockInfinityMountain() {
        assertTrue(AreaRules.isUnlocked(Stage.ULTIMATE, Area.SERVER_DESERT));
        assertFalse(AreaRules.isUnlocked(Stage.ULTIMATE, Area.INFINITY_MOUNTAIN));
    }
}
