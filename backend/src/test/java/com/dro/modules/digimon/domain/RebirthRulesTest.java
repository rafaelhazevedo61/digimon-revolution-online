package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RebirthRulesTest {

    @Test
    void calculateIvBonus_rebirth1_returns3() {
        assertEquals(3, RebirthRules.calculateIvBonus(1));
    }

    @Test
    void calculateIvBonus_rebirth5_returns15() {
        assertEquals(15, RebirthRules.calculateIvBonus(5));
    }

    @Test
    void calculateIvBonus_rebirth10_returns30() {
        assertEquals(30, RebirthRules.calculateIvBonus(10));
    }

    @Test
    void calculateIvBonus_rebirth20_cappedAt30() {
        assertEquals(30, RebirthRules.calculateIvBonus(20));
    }

    @Test
    void calculateStatMultiplier_rebirth0_returns1() {
        assertEquals(1.0, RebirthRules.calculateStatMultiplier(0));
    }

    @Test
    void calculateStatMultiplier_rebirth1_returns1_02() {
        assertEquals(1.02, RebirthRules.calculateStatMultiplier(1), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth5_returns1_10() {
        assertEquals(1.10, RebirthRules.calculateStatMultiplier(5), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth10_returns1_20() {
        assertEquals(1.20, RebirthRules.calculateStatMultiplier(10), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth20_returns1_40() {
        assertEquals(1.40, RebirthRules.calculateStatMultiplier(20), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth25_returns1_50() {
        assertEquals(1.50, RebirthRules.calculateStatMultiplier(25), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth50_returns2_00() {
        assertEquals(2.00, RebirthRules.calculateStatMultiplier(50), 0.001);
    }

    @Test
    void calculateStatMultiplier_rebirth60_cappedAt2_00() {
        assertEquals(2.00, RebirthRules.calculateStatMultiplier(60), 0.001);
    }

    @Test
    void calculateInheritedIvMinimum_previousIv100_returns90() {
        assertEquals(90, RebirthRules.calculateInheritedIvMinimum(100, 0, 1));
    }

    @Test
    void calculateInheritedIvMinimum_previousIv95_returns75() {
        assertEquals(75, RebirthRules.calculateInheritedIvMinimum(95, 0, 1));
    }

    @Test
    void calculateInheritedIvMinimum_previousIv90_returns75() {
        assertEquals(75, RebirthRules.calculateInheritedIvMinimum(90, 0, 1));
    }

    @Test
    void calculateInheritedIvMinimum_usesRarityPlusRebirthBonus() {
        // rarityMin=25, rebirthBonus=3, previousIv/2=20
        // max(25+3, 40/2) = max(28, 20) = 28
        assertEquals(28, RebirthRules.calculateInheritedIvMinimum(40, 25, 1));
    }

    @Test
    void calculateInheritedIvMinimum_usesPreviousIvProtection() {
        // rarityMin=0, rebirthBonus=3, previousIv/2=40
        // max(0+3, 80/2) = max(3, 40) = 40
        assertEquals(40, RebirthRules.calculateInheritedIvMinimum(80, 0, 1));
    }

    @Test
    void calculateInheritedIvMinimum_neverExceeds100() {
        assertEquals(100, RebirthRules.calculateInheritedIvMinimum(89, 75, 10));
    }

    @Test
    void calculateBitsCost_firstRebirth_returns10000() {
        assertEquals(10_000, RebirthRules.calculateBitsCost(0));
    }

    @Test
    void calculateBitsCost_secondRebirth_returns20000() {
        assertEquals(20_000, RebirthRules.calculateBitsCost(1));
    }

    @Test
    void calculateDataCoreCost_firstRebirth_returns1() {
        assertEquals(1, RebirthRules.calculateDataCoreCost(0));
    }

    @Test
    void calculateDataCoreCost_secondRebirth_returns2() {
        assertEquals(2, RebirthRules.calculateDataCoreCost(1));
    }

    @Test
    void isEligibleStage_baby_returnsFalse() {
        assertFalse(RebirthRules.isEligibleStage(Stage.BABY));
    }

    @Test
    void isEligibleStage_rookie_returnsFalse() {
        assertFalse(RebirthRules.isEligibleStage(Stage.ROOKIE));
    }

    @Test
    void isEligibleStage_champion_returnsTrue() {
        assertTrue(RebirthRules.isEligibleStage(Stage.CHAMPION));
    }

    @Test
    void isEligibleStage_ultimate_returnsTrue() {
        assertTrue(RebirthRules.isEligibleStage(Stage.ULTIMATE));
    }

    @Test
    void isEligibleStage_mega_returnsTrue() {
        assertTrue(RebirthRules.isEligibleStage(Stage.MEGA));
    }
}
