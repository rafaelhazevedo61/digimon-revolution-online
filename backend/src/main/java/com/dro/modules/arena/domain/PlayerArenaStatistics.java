package com.dro.modules.arena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_arena_statistics")
public class PlayerArenaStatistics {
    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "arena_points_won", nullable = false)
    private long arenaPointsWon;

    @Column(name = "arena_points_lost", nullable = false)
    private long arenaPointsLost;

    @Column(name = "arena_wins", nullable = false)
    private long arenaWins;

    @Column(name = "arena_losses", nullable = false)
    private long arenaLosses;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlayerArenaStatistics() {
    }

    public PlayerArenaStatistics(UUID playerId) {
        this.playerId = playerId;
        this.updatedAt = Instant.now();
    }

    public void recordWin(long points) {
        arenaPointsWon += Math.max(0, points);
        arenaWins++;
        updatedAt = Instant.now();
    }

    public void recordLoss(long points) {
        arenaPointsLost += Math.max(0, points);
        arenaLosses++;
        updatedAt = Instant.now();
    }

    public UUID getPlayerId() { return playerId; }
    public long getArenaPointsWon() { return arenaPointsWon; }
    public long getArenaPointsLost() { return arenaPointsLost; }
    public long getArenaWins() { return arenaWins; }
    public long getArenaLosses() { return arenaLosses; }
    public long getNetPoints() { return arenaPointsWon - arenaPointsLost; }
    public Instant getUpdatedAt() { return updatedAt; }
}

