package com.dro.shared.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameplayConfigTest {

    @Test
    void cooldownsAreEnabledByDefault() {
        GameplayConfig config = new GameplayConfig();

        assertTrue(config.isBossCooldownEnabled());
        assertTrue(config.isWorldBossCooldownEnabled());
        assertTrue(config.isClanRaidCooldownEnabled());
    }

    @Test
    void typeFlagCanDisableOnlyOneBossFlow() {
        GameplayConfig config = new GameplayConfig();
        config.getWorldBoss().setCooldownEnabled(false);

        assertFalse(config.isWorldBossCooldownEnabled());
        assertTrue(config.isClanRaidCooldownEnabled());
    }

    @Test
    void globalFlagDisablesBothBossFlows() {
        GameplayConfig config = new GameplayConfig();
        config.setBossCooldownEnabled(false);

        assertFalse(config.isWorldBossCooldownEnabled());
        assertFalse(config.isClanRaidCooldownEnabled());
    }
}
