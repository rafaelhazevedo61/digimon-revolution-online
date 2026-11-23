package com.dro.modules.achievement.api;

import java.time.Instant;
import java.util.List;

public final class AchievementDtos {
    private AchievementDtos() {}

    public record Reward(int bits, String itemCode, String itemName, int quantity) {}
    public record Entry(String code, String category, String name, String description,
                        long progress, long target, String status, Instant completedAt,
                        Instant claimedAt, Reward reward) {}
    public record Summary(int total, long completed, long available, long claimed) {}
    public record Page(Summary summary, List<Entry> achievements, Instant lastUpdatedAt, boolean updatePending) {}
    public record Claim(String message, Reward reward) {}
}
