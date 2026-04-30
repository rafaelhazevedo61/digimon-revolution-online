package com.dro.modules.mission.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MissionRulesTest {

    @Test
    void getXp_easy_returns30() {
        assertEquals(30, MissionRules.getXp(MissionType.EASY));
    }

    @Test
    void getXp_normal_returns50() {
        assertEquals(50, MissionRules.getXp(MissionType.NORMAL));
    }

    @Test
    void getXp_hard_returns100() {
        assertEquals(100, MissionRules.getXp(MissionType.HARD));
    }

    @Test
    void getCooldown_easy_returns5Seconds() {
        assertEquals(Duration.ofSeconds(5), MissionRules.getCooldown(MissionType.EASY));
    }

    @Test
    void getCooldown_normal_returns10Seconds() {
        assertEquals(Duration.ofSeconds(10), MissionRules.getCooldown(MissionType.NORMAL));
    }

    @Test
    void getCooldown_hard_returns20Seconds() {
        assertEquals(Duration.ofSeconds(20), MissionRules.getCooldown(MissionType.HARD));
    }
}
