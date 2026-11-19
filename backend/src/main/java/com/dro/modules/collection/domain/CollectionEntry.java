package com.dro.modules.collection.domain;

import com.dro.modules.digimon.domain.enums.Rarity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digimon_collection_entries", uniqueConstraints = @UniqueConstraint(name = "uk_collection_player_info_rarity", columnNames = {"player_id", "digimon_info_id", "rarity"}))
public class CollectionEntry {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "digimon_info_id", nullable = false)
    private Long digimonInfoId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rarity rarity;
    @Column(name = "source_digimon_id", nullable = false)
    private UUID sourceDigimonId;
    @Column(name = "source_event", nullable = false, length = 40)
    private String sourceEvent;
    @Column(name = "discovered_at", nullable = false)
    private LocalDateTime discoveredAt;

    protected CollectionEntry() {}
    public CollectionEntry(UUID id, UUID playerId, Long digimonInfoId, Rarity rarity, UUID sourceDigimonId, String sourceEvent) {
        this.id = id; this.playerId = playerId; this.digimonInfoId = digimonInfoId; this.rarity = rarity;
        this.sourceDigimonId = sourceDigimonId; this.sourceEvent = sourceEvent; this.discoveredAt = LocalDateTime.now();
    }
    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public Long getDigimonInfoId() { return digimonInfoId; }
    public Rarity getRarity() { return rarity; }
    public UUID getSourceDigimonId() { return sourceDigimonId; }
    public String getSourceEvent() { return sourceEvent; }
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
}
