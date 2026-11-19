package com.dro.shared.gameplay;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeekendDoubleRewardRulesTest {

    @Test
    void isActive_usesSaoPauloTimezoneAndConfiguredWindow() {
        assertFalse(WeekendDoubleRewardRules.isActive(Instant.parse("2026-08-28T21:59:59Z"))); // sexta, 18:59:59 BRT
        assertTrue(WeekendDoubleRewardRules.isActive(Instant.parse("2026-08-28T22:00:00Z"))); // sexta, 19:00 BRT
        assertTrue(WeekendDoubleRewardRules.isActive(Instant.parse("2026-08-30T23:59:59Z"))); // domingo, 20:59:59 BRT
        assertFalse(WeekendDoubleRewardRules.isActive(Instant.parse("2026-08-31T03:00:00Z"))); // segunda, 00:00 BRT
    }

    @Test
    void multipliers_doublePositiveRewardsOnlyDuringEvent() {
        Instant active = Instant.parse("2026-08-29T12:00:00Z");
        Instant inactive = Instant.parse("2026-08-27T12:00:00Z");

        assertEquals(200, WeekendDoubleRewardRules.multiplyXp(100, active));
        assertEquals(200, WeekendDoubleRewardRules.multiplyBits(100, active));
        assertEquals(100, WeekendDoubleRewardRules.multiplyXp(100, inactive));
        assertEquals(100, WeekendDoubleRewardRules.multiplyBits(100, inactive));
        assertEquals(0, WeekendDoubleRewardRules.multiplyXp(0, active));
    }
}
