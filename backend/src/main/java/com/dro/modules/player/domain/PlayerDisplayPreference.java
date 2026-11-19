package com.dro.modules.player.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_display_preferences")
public class PlayerDisplayPreference {
    @Id
    private UUID playerId;
    @Column(name = "pagination_enabled", nullable = false)
    private boolean paginationEnabled;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlayerDisplayPreference() {}

    public PlayerDisplayPreference(UUID playerId, boolean paginationEnabled, Instant updatedAt) {
        this.playerId = playerId;
        this.paginationEnabled = paginationEnabled;
        this.updatedAt = updatedAt;
    }

    public UUID getPlayerId() { return playerId; }
    public boolean isPaginationEnabled() { return paginationEnabled; }
    public void setPaginationEnabled(boolean paginationEnabled) { this.paginationEnabled = paginationEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
