package com.dro.modules.digimon.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigimonEnergyRulesTest {
    @Test
    void maxEnergyAtLevel_startsAtOneHundred() {
        assertEquals(100, DigimonEnergyRules.maxEnergyAtLevel(1));
    }

    @Test
    void maxEnergyAtLevel_addsFivePerLevel() {
        assertEquals(145, DigimonEnergyRules.maxEnergyAtLevel(10));
        assertEquals(245, DigimonEnergyRules.maxEnergyAtLevel(30));
    }

    @Test
    void maxEnergyAtLevel_clampsInvalidLevels() {
        assertEquals(100, DigimonEnergyRules.maxEnergyAtLevel(0));
        assertEquals(595, DigimonEnergyRules.maxEnergyAtLevel(150));
    }

    @Test
    void regenerationInterval_scalesWithMaximumEnergy() {
        assertEquals(300_000L, DigimonEnergyRules.regenerationIntervalMillis(100));
        assertEquals(435_000L, DigimonEnergyRules.regenerationIntervalMillis(145));
        assertEquals(735_000L, DigimonEnergyRules.regenerationIntervalMillis(245));
        assertEquals(1_785_000L, DigimonEnergyRules.regenerationIntervalMillis(595));
    }
}
