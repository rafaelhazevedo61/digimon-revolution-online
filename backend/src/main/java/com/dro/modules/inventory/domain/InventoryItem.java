package com.dro.modules.inventory.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Quantidade de um item pertencente ao inventário de um Digimon.
 *
 * <p>O inventário é vinculado ao Digimon, não diretamente ao jogador. Compras,
 * resgates e consumo de itens alteram esta quantidade dentro da transação da
 * operação que originou a mudança.</p>
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID digimonId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_definition_id")
    private ItemDefinition itemDefinition;
    @Column(nullable = false)
    private int quantity;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDigimonId() {
        return digimonId;
    }

    public void setDigimonId(UUID digimonId) {
        this.digimonId = digimonId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }

    public void setItemDefinition(ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public static class InventoryItemBuilder {
        private UUID id;
        private UUID digimonId;
        private ItemType itemType;
        private ItemDefinition itemDefinition;
        private int quantity;

        InventoryItemBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public InventoryItem.InventoryItemBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public InventoryItem.InventoryItemBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public InventoryItem.InventoryItemBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public InventoryItem.InventoryItemBuilder itemDefinition(final ItemDefinition itemDefinition) {
            this.itemDefinition = itemDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public InventoryItem.InventoryItemBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        public InventoryItem build() {
            return new InventoryItem(this.id, this.digimonId, this.itemType, this.itemDefinition, this.quantity);
        }

        @Override
        public String toString() {
            return "InventoryItem.InventoryItemBuilder(id=" + this.id + ", digimonId=" + this.digimonId + ", itemType=" + this.itemType + ", itemDefinition=" + this.itemDefinition + ", quantity=" + this.quantity + ")";
        }
    }

    public static InventoryItem.InventoryItemBuilder builder() {
        return new InventoryItem.InventoryItemBuilder();
    }

    public InventoryItem() {
    }

    public InventoryItem(final UUID id, final UUID digimonId, final ItemType itemType, final ItemDefinition itemDefinition, final int quantity) {
        this.id = id;
        this.digimonId = digimonId;
        this.itemType = itemType;
        this.itemDefinition = itemDefinition;
        this.quantity = quantity;
    }
}
