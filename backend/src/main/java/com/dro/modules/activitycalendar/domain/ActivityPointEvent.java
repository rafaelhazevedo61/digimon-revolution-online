package com.dro.modules.activitycalendar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "activity_point_events")
public class ActivityPointEvent {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;
    @Column(nullable = false, length = 40)
    private String source;
    @Column(name = "source_reference_id", nullable = false, length = 120)
    private String sourceReferenceId;
    @Column(nullable = false)
    private int points;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ActivityPointEvent() {}
    public ActivityPointEvent(UUID id, UUID playerId, LocalDate activityDate, String source,
                              String sourceReferenceId, int points, String metadata, Instant createdAt) {
        this.id = id; this.playerId = playerId; this.activityDate = activityDate; this.source = source;
        this.sourceReferenceId = sourceReferenceId; this.points = points; this.metadata = metadata; this.createdAt = createdAt;
    }
    public static ActivityPointEvent create(UUID playerId, LocalDate date, String source, String reference, int points, String metadata, Instant now) {
        return new ActivityPointEvent(UUID.randomUUID(), playerId, date, source, reference, points, metadata, now);
    }
    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public LocalDate getActivityDate() { return activityDate; }
    public String getSource() { return source; }
    public String getSourceReferenceId() { return sourceReferenceId; }
    public int getPoints() { return points; }
    public String getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
