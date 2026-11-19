package com.dro.modules.activitycalendar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_calendar_monthly")
public class ActivityCalendarMonthly {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;
    @Column(name = "total_days", nullable = false)
    private int totalDays;
    @Column(name = "claimed_days", nullable = false)
    private int claimedDays;
    @Column(name = "monthly_completion_eligible_at")
    private Instant monthlyCompletionEligibleAt;
    @Column(name = "monthly_reward_claimed_at")
    private Instant monthlyRewardClaimedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ActivityCalendarMonthly() {}

    public ActivityCalendarMonthly(UUID id, UUID playerId, String yearMonth, int totalDays, int claimedDays,
                                   Instant monthlyCompletionEligibleAt, Instant monthlyRewardClaimedAt,
                                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.yearMonth = yearMonth;
        this.totalDays = totalDays;
        this.claimedDays = claimedDays;
        this.monthlyCompletionEligibleAt = monthlyCompletionEligibleAt;
        this.monthlyRewardClaimedAt = monthlyRewardClaimedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ActivityCalendarMonthly create(UUID playerId, String yearMonth, int totalDays, Instant now) {
        return new ActivityCalendarMonthly(UUID.randomUUID(), playerId, yearMonth, totalDays, 0, null, null, now, now);
    }

    public void incrementClaimedDays(Instant now) {
        if (claimedDays < totalDays) claimedDays++;
        if (claimedDays == totalDays && monthlyCompletionEligibleAt == null) monthlyCompletionEligibleAt = now;
        updatedAt = now;
    }

    public boolean isEligible() { return monthlyCompletionEligibleAt != null; }
    public boolean isRewardClaimed() { return monthlyRewardClaimedAt != null; }
    public void markRewardClaimed(Instant now) { monthlyRewardClaimedAt = now; updatedAt = now; }
    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public String getYearMonth() { return yearMonth; }
    public int getTotalDays() { return totalDays; }
    public int getClaimedDays() { return claimedDays; }
    public Instant getMonthlyCompletionEligibleAt() { return monthlyCompletionEligibleAt; }
    public Instant getMonthlyRewardClaimedAt() { return monthlyRewardClaimedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
