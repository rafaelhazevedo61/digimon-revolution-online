package com.dro.modules.player.domain;

import java.util.Arrays;
import java.util.List;

public enum VipPlan {
    ONE_DAY(1, 199),
    SEVEN_DAYS(7, 690),
    THIRTY_DAYS(30, 1490);

    private final int days;
    private final int priceInCents;

    VipPlan(int days, int priceInCents) {
        this.days = days;
        this.priceInCents = priceInCents;
    }

    public int getDays() {
        return days;
    }

    public static List<Offer> offers() {
        return Arrays.stream(values())
                .map(plan -> new Offer(plan, plan.days, plan.priceInCents))
                .toList();
    }

    public record Offer(VipPlan plan, int days, int priceInCents) {}
}
