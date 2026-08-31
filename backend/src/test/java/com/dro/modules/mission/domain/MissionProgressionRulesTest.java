package com.dro.modules.mission.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissionProgressionRulesTest {

    @Test
    void rewardMultiplier_startsAtOne() {
        assertEquals(1.0, MissionProgressionRules.rewardMultiplier(0));
    }

    @Test
    void rewardMultiplier_reachesTwoAtTheCap() {
        assertEquals(2.0, MissionProgressionRules.rewardMultiplier(100));
        assertEquals(2.0, MissionProgressionRules.rewardMultiplier(101));
        assertEquals(2.0, MissionProgressionRules.rewardMultiplier(Integer.MAX_VALUE));
    }

    @Test
    void rewardMultiplier_normalizesInvalidNegativeCounts() {
        assertEquals(1.0, MissionProgressionRules.rewardMultiplier(-1));
    }

    @Test
    void nextCompletionCount_saturatesAtTheCap() {
        assertEquals(1, MissionProgressionRules.nextCompletionCount(0));
        assertEquals(100, MissionProgressionRules.nextCompletionCount(99));
        assertEquals(100, MissionProgressionRules.nextCompletionCount(100));
        assertEquals(100, MissionProgressionRules.nextCompletionCount(Integer.MAX_VALUE));
    }
}
