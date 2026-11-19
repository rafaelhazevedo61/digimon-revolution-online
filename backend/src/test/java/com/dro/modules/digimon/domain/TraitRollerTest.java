package com.dro.modules.digimon.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraitRollerTest {

    @Test
    void calculateRebirthTraitChance_rebirth1_returns12Percent() {
        assertEquals(0.12, TraitRoller.calculateRebirthTraitChance(1), 0.001);
    }

    @Test
    void calculateRebirthTraitChance_rebirth15_returns40Percent() {
        assertEquals(0.40, TraitRoller.calculateRebirthTraitChance(15), 0.001);
    }

    @Test
    void calculateRebirthTraitChance_rebirth25_returns60Percent() {
        assertEquals(0.60, TraitRoller.calculateRebirthTraitChance(25), 0.001);
    }

    @Test
    void calculateRebirthTraitChance_rebirth30_cappedAt60Percent() {
        assertEquals(0.60, TraitRoller.calculateRebirthTraitChance(30), 0.001);
    }
}
