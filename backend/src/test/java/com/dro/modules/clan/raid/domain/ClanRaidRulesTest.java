package com.dro.modules.clan.raid.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClanRaidRulesTest {

    @Test
    void attackCooldownMinutes_usesFiveMinutesAsDefault() {
        assertEquals(5, ClanRaidRules.attackCooldownMinutes(0));
        assertEquals(5, ClanRaidRules.attackCooldownMinutes(-1));
    }

    @Test
    void attackCooldownMinutes_preservesPositiveConfiguredValue() {
        assertEquals(15, ClanRaidRules.attackCooldownMinutes(15));
    }

    @Test
    void calculateDamage_neverExceedsMaxPercent() {
        int maxHp = 10000;

        int damage = ClanRaidRules.calculateDamage(maxHp, 100);
        int maxExpected = (int) Math.round(maxHp * 0.15);

        assertTrue(damage >= 1, "Damage should be at least 1");
        assertTrue(damage <= maxExpected, "Damage should not exceed 15% of max HP");
    }

    @Test
    void calculateDamage_lowWinChanceStillDealsSmallDamage() {
        int maxHp = 10000;
        int damage = ClanRaidRules.calculateDamage(maxHp, 5);

        assertEquals(100, damage, "Low win chance should deal 1% damage");
    }

    @Test
    void hitXpAndBits_useDefeatPercent() {
        assertEquals(5, ClanRaidRules.hitXp(100, 5));
        assertEquals(10, ClanRaidRules.hitBits(100, 10));
    }
}
