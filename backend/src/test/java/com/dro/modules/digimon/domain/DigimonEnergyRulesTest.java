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
}
