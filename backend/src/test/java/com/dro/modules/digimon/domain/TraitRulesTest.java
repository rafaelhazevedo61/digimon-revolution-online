package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Trait;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraitRulesTest {

    @Test
    void getXpMultiplier_fastLearner_returns1_10() {
        assertEquals(1.10, TraitRules.getXpMultiplier(Trait.FAST_LEARNER));
    }

    @Test
    void getXpMultiplier_otherTrait_returns1() {
        assertEquals(1.0, TraitRules.getXpMultiplier(Trait.VITALITY));
    }

    @Test
    void getXpMultiplier_null_returns1() {
        assertEquals(1.0, TraitRules.getXpMultiplier(null));
    }

    @Test
    void getHpMultiplier_vitality_returns1_10() {
        assertEquals(1.10, TraitRules.getHpMultiplier(Trait.VITALITY));
    }

    @Test
    void getHpMultiplier_otherTrait_returns1() {
        assertEquals(1.0, TraitRules.getHpMultiplier(Trait.BERSERKER));
    }

    @Test
    void getAttackMultiplier_berserker_returns1_10() {
        assertEquals(1.10, TraitRules.getAttackMultiplier(Trait.BERSERKER));
    }

    @Test
    void getAttackMultiplier_otherTrait_returns1() {
        assertEquals(1.0, TraitRules.getAttackMultiplier(Trait.VITALITY));
    }

    @Test
    void getDefenseMultiplier_ironBody_returns1_10() {
        assertEquals(1.10, TraitRules.getDefenseMultiplier(Trait.IRON_BODY));
    }

    @Test
    void getDefenseMultiplier_otherTrait_returns1() {
        assertEquals(1.0, TraitRules.getDefenseMultiplier(Trait.BERSERKER));
    }

    @Test
    void getMaxEnergyBonus_energetic_returns5() {
        assertEquals(5, TraitRules.getMaxEnergyBonus(Trait.ENERGETIC));
    }

    @Test
    void getMaxEnergyBonus_otherTrait_returns0() {
        assertEquals(0, TraitRules.getMaxEnergyBonus(Trait.VITALITY));
    }
}
