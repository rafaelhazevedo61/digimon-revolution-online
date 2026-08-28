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
    void calculateDamage_staysWithinClanRaidRange() {
        int maxHp = 10000;

        int damage = ClanRaidRules.calculateDamage(maxHp, 100);
        int minExpected = (int) Math.round(maxHp * 0.0075);
        int maxExpected = (int) Math.round(maxHp * 0.0775);

        assertTrue(damage >= minExpected, "Damage should respect the Clan Raid minimum");
        assertTrue(damage <= maxExpected, "Damage should stay within the Clan Raid maximum");
    }

    @Test
    void calculateDamage_lowWinChanceStillDealsUsefulDamage() {
        int maxHp = 50000;
        int damage = ClanRaidRules.calculateDamage(maxHp, 5);

        assertTrue(damage >= 375 && damage <= 550,
                "A 50,000 HP raid with 5% chance should deal between 375 and 550 damage");
    }

    @Test
    void hitXpAndBits_useDefeatPercent() {
        assertEquals(5, ClanRaidRules.hitXp(100, 5));
        assertEquals(10, ClanRaidRules.hitBits(100, 10));
    }
}
