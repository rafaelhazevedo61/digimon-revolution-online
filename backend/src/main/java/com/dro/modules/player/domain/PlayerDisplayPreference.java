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
    @Column(name = "shortcut_routes", nullable = false, columnDefinition = "TEXT")
    private String shortcutRoutes;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlayerDisplayPreference() {}

    public PlayerDisplayPreference(UUID playerId, boolean paginationEnabled, Instant updatedAt) {
        this(playerId, paginationEnabled, "", updatedAt);
    }

    public PlayerDisplayPreference(UUID playerId, boolean paginationEnabled, String shortcutRoutes, Instant updatedAt) {
        this.playerId = playerId;
        this.paginationEnabled = paginationEnabled;
        this.shortcutRoutes = shortcutRoutes == null ? "" : shortcutRoutes;
        this.updatedAt = updatedAt;
    }

    public UUID getPlayerId() { return playerId; }
    public boolean isPaginationEnabled() { return paginationEnabled; }
    public String getShortcutRoutes() { return shortcutRoutes; }
    public void setShortcutRoutes(String shortcutRoutes) { this.shortcutRoutes = shortcutRoutes == null ? "" : shortcutRoutes; }
    public void setPaginationEnabled(boolean paginationEnabled) { this.paginationEnabled = paginationEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
