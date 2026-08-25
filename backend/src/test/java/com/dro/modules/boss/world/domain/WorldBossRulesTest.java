package com.dro.modules.boss.world.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldBossRulesTest {

    @Test
    void attackCooldownMinutesUsesFiveMinutesAsDefault() {
        assertEquals(5, WorldBossRules.attackCooldownMinutes(0));
        assertEquals(5, WorldBossRules.attackCooldownMinutes(-1));
    }

    @Test
    void attackCooldownMinutesPreservesPositiveConfiguredValue() {
        assertEquals(1, WorldBossRules.attackCooldownMinutes(1));
        assertEquals(15, WorldBossRules.attackCooldownMinutes(15));
    }

    @Test
    void dailyAttacksRemainingUsesConfiguredLimit() {
        assertEquals(10, WorldBossRules.dailyAttacksRemaining(0, 10));
        assertEquals(1, WorldBossRules.dailyAttacksRemaining(9, 10));
        assertEquals(0, WorldBossRules.dailyAttacksRemaining(10, 10));
        assertEquals(0, WorldBossRules.dailyAttacksRemaining(11, 10));
    }

    @Test
    void dailyLimitReachedUsesConfiguredLimit() {
        assertFalse(WorldBossRules.dailyLimitReached(9, 10));
        assertTrue(WorldBossRules.dailyLimitReached(10, 10));
    }
}
