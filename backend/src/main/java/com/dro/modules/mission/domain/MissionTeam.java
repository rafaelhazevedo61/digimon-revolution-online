package com.dro.modules.mission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mission_teams")
public class MissionTeam {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(name = "digimon_1_id", nullable = false)
    private UUID digimon1Id;

    @Column(name = "digimon_2_id")
    private UUID digimon2Id;

    @Column(name = "digimon_3_id")
    private UUID digimon3Id;

    @Column(name = "captain_digimon_id", nullable = false)
    private UUID captainDigimonId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected MissionTeam() {
        // JPA
    }

    public MissionTeam(UUID playerId, String name, List<UUID> digimonIds, UUID captainDigimonId) {
        if (digimonIds == null || digimonIds.isEmpty() || digimonIds.size() > 3) {
            throw new IllegalArgumentException("Um time precisa ter entre 1 e 3 Digimons");
        }
        this.playerId = playerId;
        this.name = name;
        this.digimon1Id = digimonIds.get(0);
        this.digimon2Id = digimonIds.size() > 1 ? digimonIds.get(1) : null;
        this.digimon3Id = digimonIds.size() > 2 ? digimonIds.get(2) : null;
        this.captainDigimonId = captainDigimonId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String name, List<UUID> digimonIds, UUID captainDigimonId) {
        if (digimonIds == null || digimonIds.isEmpty() || digimonIds.size() > 3) {
            throw new IllegalArgumentException("Um time precisa ter entre 1 e 3 Digimons");
        }
        this.name = name;
        this.digimon1Id = digimonIds.get(0);
        this.digimon2Id = digimonIds.size() > 1 ? digimonIds.get(1) : null;
        this.digimon3Id = digimonIds.size() > 2 ? digimonIds.get(2) : null;
        this.captainDigimonId = captainDigimonId;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public UUID getDigimon1Id() {
        return digimon1Id;
    }

    public UUID getDigimon2Id() {
        return digimon2Id;
    }

    public UUID getDigimon3Id() {
        return digimon3Id;
    }

    public UUID getCaptainDigimonId() {
        return captainDigimonId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<UUID> getDigimonIds() {
        List<UUID> ids = new ArrayList<>();
        if (digimon1Id != null) ids.add(digimon1Id);
        if (digimon2Id != null) ids.add(digimon2Id);
        if (digimon3Id != null) ids.add(digimon3Id);
        return ids;
    }
}
