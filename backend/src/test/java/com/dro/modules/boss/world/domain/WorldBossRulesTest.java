package com.dro.modules.boss.world.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
