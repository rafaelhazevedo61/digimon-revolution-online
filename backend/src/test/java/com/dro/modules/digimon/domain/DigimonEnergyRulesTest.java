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
        assertEquals(30_000_000L / 100, DigimonEnergyRules.regenerationIntervalMillis(100));
        assertEquals(30_000_000L / 145, DigimonEnergyRules.regenerationIntervalMillis(145));
        assertEquals(30_000_000L / 245, DigimonEnergyRules.regenerationIntervalMillis(245));
        assertEquals(30_000_000L / 595, DigimonEnergyRules.regenerationIntervalMillis(595));
    }

    @Test
    void energyRecovered_completesEveryCapacityInEightHoursAndTwentyMinutes() {
        long fullDuration = DigimonEnergyRules.FULL_REGENERATION_DURATION_MILLIS;
        assertEquals(100, DigimonEnergyRules.energyRecovered(fullDuration, 100));
        assertEquals(145, DigimonEnergyRules.energyRecovered(fullDuration, 145));
        assertEquals(245, DigimonEnergyRules.energyRecovered(fullDuration, 245));
        assertEquals(595, DigimonEnergyRules.energyRecovered(fullDuration, 595));
    }
}
