package com.dro.modules.achievement.application;

import com.dro.modules.player.domain.VipPassRules;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class VipPassRulesTest {
    private final Instant now = Instant.parse("2026-11-19T20:00:00Z");

    @Test void newVipStartsAtUseTime() {
        assertEquals(now.plus(Duration.ofHours(24)), VipPassRules.extendOneDay(null, now));
    }
    @Test void expiredVipStartsAtUseTime() {
        assertEquals(now.plus(Duration.ofHours(24)), VipPassRules.extendOneDay(now.minusSeconds(1), now));
    }
    @Test void activeVipKeepsAllItsRemainingTime() {
        Instant expiry = now.plus(Duration.ofDays(7));
        assertEquals(expiry.plus(Duration.ofHours(24)), VipPassRules.extendOneDay(expiry, now));
    }
    @Test void multiplePassesAccumulate() {
        Instant first = VipPassRules.extendOneDay(null, now);
        assertEquals(now.plus(Duration.ofHours(48)), VipPassRules.extendOneDay(first, now));
    }
}
