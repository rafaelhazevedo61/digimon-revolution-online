package com.dro.modules.clan.storage.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clan_storage_history")
public class ClanStorageHistory {
    @Id
    private UUID id;

    @Column(name = "clan_id", nullable = false)
    private UUID clanId;

    @Column(name = "actor_player_id", nullable = false)
    private UUID actorPlayerId;

    @Column(name = "actor_username", nullable = false, length = 80)
    private String actorUsername;

    @Column(nullable = false, length = 20)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_definition_id", nullable = false)
    private ItemDefinition itemDefinition;

    @Column(name = "item_code", nullable = false, length = 80)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ClanStorageHistory() {
    }

    private ClanStorageHistory(UUID id, UUID clanId, UUID actorPlayerId, String actorUsername,
                               String action, ItemDefinition itemDefinition, String itemCode,
                               String itemName, int quantity, LocalDateTime createdAt) {
        this.id = id;
        this.clanId = clanId;
        this.actorPlayerId = actorPlayerId;
        this.actorUsername = actorUsername;
        this.action = action;
        this.itemDefinition = itemDefinition;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public static ClanStorageHistory create(UUID clanId, UUID actorPlayerId, String actorUsername,
                                            String action, ItemDefinition itemDefinition, int quantity) {
        return new ClanStorageHistory(
                UUID.randomUUID(), clanId, actorPlayerId, actorUsername, action,
                itemDefinition, itemDefinition.getCode(), itemDefinition.getName(), quantity,
                LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getClanId() {
        return clanId;
    }

    public UUID getActorPlayerId() {
        return actorPlayerId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getAction() {
        return action;
    }

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
