package com.dro.modules.inventory.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Quantidade de um item pertencente ao inventário de um jogador.
 *
 * <p>O inventário é global por jogador. Compras, resgates e consumo de itens
 * alteram esta quantidade dentro da transação da operação que originou a mudança.</p>
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID playerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_definition_id")
    private ItemDefinition itemDefinition;
    @Column(nullable = false)
    private int quantity;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    /** @deprecated O inventário não possui mais vínculo com Digimon. */
    @Deprecated
    public UUID getDigimonId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    public ItemDefinition getItemDefinition() { return itemDefinition; }
    public void setItemDefinition(ItemDefinition itemDefinition) { this.itemDefinition = itemDefinition; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public static class InventoryItemBuilder {
        private UUID id;
        private UUID playerId;
        private ItemType itemType;
        private ItemDefinition itemDefinition;
        private int quantity;
        InventoryItemBuilder() {}
        public InventoryItemBuilder id(final UUID id) { this.id = id; return this; }
        public InventoryItemBuilder playerId(final UUID playerId) { this.playerId = playerId; return this; }
        /** @deprecated Use playerId; mantido somente para compatibilidade de fixtures antigas. */
        @Deprecated
        public InventoryItemBuilder digimonId(final UUID digimonId) { this.playerId = digimonId; return this; }
        public InventoryItemBuilder itemType(final ItemType itemType) { this.itemType = itemType; return this; }
        public InventoryItemBuilder itemDefinition(final ItemDefinition itemDefinition) { this.itemDefinition = itemDefinition; return this; }
        public InventoryItemBuilder quantity(final int quantity) { this.quantity = quantity; return this; }
        public InventoryItem build() { return new InventoryItem(id, playerId, itemType, itemDefinition, quantity); }
        @Override public String toString() { return "InventoryItem.InventoryItemBuilder(id=" + id + ", playerId=" + playerId + ", itemType=" + itemType + ", itemDefinition=" + itemDefinition + ", quantity=" + quantity + ")"; }
    }

    public static InventoryItemBuilder builder() { return new InventoryItemBuilder(); }
    public InventoryItem() {}
    public InventoryItem(UUID id, UUID playerId, ItemType itemType, ItemDefinition itemDefinition, int quantity) {
        this.id = id;
        this.playerId = playerId;
        this.itemType = itemType;
        this.itemDefinition = itemDefinition;
        this.quantity = quantity;
    }
}
