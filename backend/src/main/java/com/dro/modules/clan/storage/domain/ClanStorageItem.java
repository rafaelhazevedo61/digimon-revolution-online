package com.dro.modules.clan.storage.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clan_storage_items")
public class ClanStorageItem {
    @Id
    private UUID id;

    @Column(name = "clan_id", nullable = false)
    private UUID clanId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_definition_id", nullable = false)
    private ItemDefinition itemDefinition;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ClanStorageItem() {
    }

    private ClanStorageItem(UUID id, UUID clanId, ItemDefinition itemDefinition, int quantity,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clanId = clanId;
        this.itemDefinition = itemDefinition;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClanStorageItem create(UUID clanId, ItemDefinition itemDefinition, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        return new ClanStorageItem(UUID.randomUUID(), clanId, itemDefinition, quantity, now, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getClanId() {
        return clanId;
    }

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
