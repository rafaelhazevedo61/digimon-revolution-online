package com.dro.modules.digimon.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigimonLevelRulesTest {

    @Test
    void xpToNextLevel_returnsLevelTimes100() {
        assertEquals(100, DigimonLevelRules.xpToNextLevel(1));
        assertEquals(500, DigimonLevelRules.xpToNextLevel(5));
        assertEquals(5000, DigimonLevelRules.xpToNextLevel(50));
    }

    @Test
    void xpToNextLevel_atMaxLevel_returnsZero() {
        assertEquals(0, DigimonLevelRules.xpToNextLevel(100));
    }

    @Test
    void xpToNextLevel_aboveMaxLevel_returnsZero() {
        assertEquals(0, DigimonLevelRules.xpToNextLevel(150));
    }

    @Test
    void totalXpToReachLevel_level1_returnsZero() {
        assertEquals(0, DigimonLevelRules.totalXpToReachLevel(1));
    }

    @Test
    void totalXpToReachLevel_belowLevel1_returnsZero() {
        assertEquals(0, DigimonLevelRules.totalXpToReachLevel(0));
        assertEquals(0, DigimonLevelRules.totalXpToReachLevel(-1));
    }

    @Test
    void totalXpToReachLevel_level2_returns100() {
        assertEquals(100, DigimonLevelRules.totalXpToReachLevel(2));
    }

    @Test
    void totalXpToReachLevel_level3_returns300() {
        // level 1->2 = 100, level 2->3 = 200
        assertEquals(300, DigimonLevelRules.totalXpToReachLevel(3));
    }

    @Test
    void getExperienceTable_returns100Entries() {
        var table = DigimonLevelRules.getExperienceTable();
        assertEquals(100, table.size());
        assertEquals(1, table.get(0).level());
        assertEquals(100, table.get(99).level());
    }
}
