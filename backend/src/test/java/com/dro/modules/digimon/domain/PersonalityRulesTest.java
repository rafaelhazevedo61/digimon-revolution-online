package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Personality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonalityRulesTest {

    @Test
    void getHpMultiplier_durable_returns1_10() {
        assertEquals(1.10, PersonalityRules.getHpMultiplier(Personality.DURABLE));
    }

    @Test
    void getHpMultiplier_fighter_returns1() {
        assertEquals(1.0, PersonalityRules.getHpMultiplier(Personality.FIGHTER));
    }

    @Test
    void getAttackMultiplier_fighter_returns1_10() {
        assertEquals(1.10, PersonalityRules.getAttackMultiplier(Personality.FIGHTER));
    }

    @Test
    void getAttackMultiplier_brainy_returns1_05() {
        assertEquals(1.05, PersonalityRules.getAttackMultiplier(Personality.BRAINY));
    }

    @Test
    void getAttackMultiplier_nimble_returns1_05() {
        assertEquals(1.05, PersonalityRules.getAttackMultiplier(Personality.NIMBLE));
    }

    @Test
    void getDefenseMultiplier_defender_returns1_10() {
        assertEquals(1.10, PersonalityRules.getDefenseMultiplier(Personality.DEFENDER));
    }

    @Test
    void getDefenseMultiplier_nimble_returns1_05() {
        assertEquals(1.05, PersonalityRules.getDefenseMultiplier(Personality.NIMBLE));
    }

    @Test
    void getXpMultiplier_lively_returns1_10() {
        assertEquals(1.10, PersonalityRules.getXpMultiplier(Personality.LIVELY));
    }

    @Test
    void getXpMultiplier_brainy_returns1_05() {
        assertEquals(1.05, PersonalityRules.getXpMultiplier(Personality.BRAINY));
    }

    @Test
    void getXpMultiplier_fighter_returns1() {
        assertEquals(1.0, PersonalityRules.getXpMultiplier(Personality.FIGHTER));
    }
}
