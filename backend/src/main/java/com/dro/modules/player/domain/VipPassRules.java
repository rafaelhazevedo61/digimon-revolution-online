package com.dro.modules.player.domain;

import java.time.Duration;
import java.time.Instant;

public final class VipPassRules {
    private VipPassRules() {}

    public static Instant extendOneDay(Instant currentExpiry, Instant now) {
        Instant startsAt = currentExpiry != null && currentExpiry.isAfter(now) ? currentExpiry : now;
        return startsAt.plus(Duration.ofDays(1));
    }
}
