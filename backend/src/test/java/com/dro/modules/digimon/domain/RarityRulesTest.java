package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Rarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RarityRulesTest {

    @Test
    void getStatMultiplier_common_returns1() {
        assertEquals(1.0, RarityRules.getStatMultiplier(Rarity.COMMON));
    }

    @Test
    void getStatMultiplier_rare_returns1_1() {
        assertEquals(1.1, RarityRules.getStatMultiplier(Rarity.RARE));
    }

    @Test
    void getStatMultiplier_epic_returns1_25() {
        assertEquals(1.25, RarityRules.getStatMultiplier(Rarity.EPIC));
    }

    @Test
    void getStatMultiplier_legendary_returns1_5() {
        assertEquals(1.5, RarityRules.getStatMultiplier(Rarity.LEGENDARY));
    }

    @Test
    void getXpMultiplier_common_returns1() {
        assertEquals(1.0, RarityRules.getXpMultiplier(Rarity.COMMON));
    }

    @Test
    void getXpMultiplier_rare_returns1_05() {
        assertEquals(1.05, RarityRules.getXpMultiplier(Rarity.RARE));
    }

    @Test
    void getXpMultiplier_epic_returns1_1() {
        assertEquals(1.1, RarityRules.getXpMultiplier(Rarity.EPIC));
    }

    @Test
    void getXpMultiplier_legendary_returns1_2() {
        assertEquals(1.2, RarityRules.getXpMultiplier(Rarity.LEGENDARY));
    }

    @Test
    void getMinimumIv_common_returns0() {
        assertEquals(0, RarityRules.getMinimumIv(Rarity.COMMON));
    }

    @Test
    void getMinimumIv_rare_returns25() {
        assertEquals(25, RarityRules.getMinimumIv(Rarity.RARE));
    }

    @Test
    void getMinimumIv_epic_returns50() {
        assertEquals(50, RarityRules.getMinimumIv(Rarity.EPIC));
    }

    @Test
    void getMinimumIv_legendary_returns75() {
        assertEquals(75, RarityRules.getMinimumIv(Rarity.LEGENDARY));
    }
}
