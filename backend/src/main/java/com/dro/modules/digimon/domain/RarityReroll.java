package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Rarity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digimon_rarity_rerolls")
public class RarityReroll {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;
    @Enumerated(EnumType.STRING)
    @Column(name = "current_rarity", nullable = false)
    private Rarity currentRarity;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_rarity", nullable = false)
    private Rarity newRarity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RerollStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected RarityReroll() { }

    public RarityReroll(UUID id, UUID playerId, UUID digimonId, Rarity currentRarity, Rarity newRarity) {
        this.id = id;
        this.playerId = playerId;
        this.digimonId = digimonId;
        this.currentRarity = currentRarity;
        this.newRarity = newRarity;
        this.status = RerollStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public UUID getDigimonId() { return digimonId; }
    public Rarity getCurrentRarity() { return currentRarity; }
    public Rarity getNewRarity() { return newRarity; }
    public RerollStatus getStatus() { return status; }
    public void accept() { status = RerollStatus.ACCEPTED; completedAt = LocalDateTime.now(); }
    public void keep() { status = RerollStatus.KEPT; completedAt = LocalDateTime.now(); }
}
