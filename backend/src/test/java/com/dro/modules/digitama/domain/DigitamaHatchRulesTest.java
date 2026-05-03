package com.dro.modules.digitama.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitamaHatchRulesTest {

    @Test
    void getPossibleBabies_fire_returnsBotamonAndPunimon() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.FIRE);
        assertEquals(2, babies.size());
        assertTrue(babies.contains("Botamon"));
        assertTrue(babies.contains("Punimon"));
    }

    @Test
    void getPossibleBabies_water_returnsPichimonAndPoyomon() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.WATER);
        assertEquals(2, babies.size());
        assertTrue(babies.contains("Pichimon"));
        assertTrue(babies.contains("Poyomon"));
    }

    @Test
    void getPossibleBabies_nature_returnsPabumonAndYuramon() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.NATURE);
        assertEquals(2, babies.size());
        assertTrue(babies.contains("Pabumon"));
        assertTrue(babies.contains("Yuramon"));
    }

    @Test
    void rollBabyName_fire_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.FIRE);
            assertTrue(name.equals("Botamon") || name.equals("Punimon"),
                    "Expected Botamon or Punimon but got: " + name);
        }
    }

    @Test
    void rollBabyName_water_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.WATER);
            assertTrue(name.equals("Pichimon") || name.equals("Poyomon"),
                    "Expected Pichimon or Poyomon but got: " + name);
        }
    }

    @Test
    void rollBabyName_nature_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.NATURE);
            assertTrue(name.equals("Pabumon") || name.equals("Yuramon"),
                    "Expected Pabumon or Yuramon but got: " + name);
        }
    }

    @Test
    void toDigitamaType_fire() {
        assertEquals(DigitamaType.FIRE, DigitamaHatchRules.toDigitamaType(ItemType.DIGITAMA_FIRE));
    }

    @Test
    void toDigitamaType_water() {
        assertEquals(DigitamaType.WATER, DigitamaHatchRules.toDigitamaType(ItemType.DIGITAMA_WATER));
    }

    @Test
    void toDigitamaType_nature() {
        assertEquals(DigitamaType.NATURE, DigitamaHatchRules.toDigitamaType(ItemType.DIGITAMA_NATURE));
    }

    @Test
    void toDigitamaType_throwsForNonDigitama() {
        assertThrows(IllegalArgumentException.class,
                () -> DigitamaHatchRules.toDigitamaType(ItemType.TRAINING_STONE));
    }
}
