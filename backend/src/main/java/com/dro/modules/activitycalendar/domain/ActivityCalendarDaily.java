package com.dro.modules.activitycalendar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "activity_calendar_daily")
public class ActivityCalendarDaily {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;
    @Column(nullable = false)
    private int points;
    @Column(name = "goal_reached_at")
    private Instant goalReachedAt;
    @Column(name = "reward_claimed_at")
    private Instant rewardClaimedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ActivityCalendarDaily() {}
    public ActivityCalendarDaily(UUID id, UUID playerId, LocalDate activityDate, String yearMonth, int points,
                                 Instant goalReachedAt, Instant rewardClaimedAt, Instant createdAt, Instant updatedAt) {
        this.id = id; this.playerId = playerId; this.activityDate = activityDate; this.yearMonth = yearMonth;
        this.points = points; this.goalReachedAt = goalReachedAt; this.rewardClaimedAt = rewardClaimedAt;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }
    public static ActivityCalendarDaily create(UUID playerId, LocalDate date, Instant now) {
        return new ActivityCalendarDaily(UUID.randomUUID(), playerId, date, date.toString().substring(0, 7), 0, null, null, now, now);
    }
    public void addPoints(int amount, int goal, Instant now) {
        points += amount;
        if (points >= goal && goalReachedAt == null) goalReachedAt = now;
        updatedAt = now;
    }
    public boolean isGoalReached() { return goalReachedAt != null; }
    public boolean isRewardClaimed() { return rewardClaimedAt != null; }
    public void markRewardClaimed(Instant now) { rewardClaimedAt = now; updatedAt = now; }
    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public LocalDate getActivityDate() { return activityDate; }
    public String getYearMonth() { return yearMonth; }
    public int getPoints() { return points; }
    public Instant getGoalReachedAt() { return goalReachedAt; }
    public Instant getRewardClaimedAt() { return rewardClaimedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
