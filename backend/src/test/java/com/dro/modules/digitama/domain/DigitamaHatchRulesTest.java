package com.dro.modules.digitama.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitamaHatchRulesTest {

    @Test
    void getPossibleBabies_fire_returnsOnlyFireBabies() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.FIRE);
        assertEquals(5, babies.size());
        assertTrue(babies.containsAll(List.of("Bombmon", "Bommon", "Jyarimon", "Mokumon", "Peti Meramon")));
    }

    @Test
    void getPossibleBabies_water_returnsOnlyWaterBabies() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.WATER);
        assertEquals(8, babies.size());
        assertTrue(babies.containsAll(List.of("Punimon", "Pichimon", "Bubbmon", "Fukamon", "Kekomon", "Pitchmon", "Pururumon", "Puyomon")));
    }

    @Test
    void getPossibleBabies_nature_returnsOnlyWoodBabies() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.NATURE);
        assertEquals(4, babies.size());
        assertTrue(babies.containsAll(List.of("Yuramon", "Leafmon", "Nyokimon", "Popomon")));
    }

    @Test
    void getPossibleBabies_steel_returnsOnlyMetalBabies() {
        List<String> babies = DigitamaHatchRules.getPossibleBabies(DigitamaType.STEEL);
        assertEquals(1, babies.size());
        assertEquals(List.of("MetalKoromon"), babies);
    }

    @Test
    void rollBabyName_fire_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.FIRE);
            assertTrue(List.of("Bombmon", "Bommon", "Jyarimon", "Mokumon", "Peti Meramon").contains(name),
                    "Expected a fire BABY but got: " + name);
        }
    }

    @Test
    void rollBabyName_water_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.WATER);
            assertTrue(List.of("Punimon", "Pichimon", "Bubbmon", "Fukamon", "Kekomon", "Pitchmon", "Pururumon", "Puyomon").contains(name),
                    "Expected a water BABY but got: " + name);
        }
    }

    @Test
    void rollBabyName_nature_returnsValidBaby() {
        for (int i = 0; i < 50; i++) {
            String name = DigitamaHatchRules.rollBabyName(DigitamaType.NATURE);
            assertTrue(List.of("Yuramon", "Leafmon", "Nyokimon", "Popomon").contains(name),
                    "Expected a wood BABY but got: " + name);
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
    void toDigitamaType_steel() {
        assertEquals(DigitamaType.STEEL, DigitamaHatchRules.toDigitamaType(ItemType.DIGITAMA_STEEL));
    }

    @Test
    void toDigitamaType_throwsForNonDigitama() {
        assertThrows(IllegalArgumentException.class,
                () -> DigitamaHatchRules.toDigitamaType(ItemType.TRAINING_STONE));
    }
}
