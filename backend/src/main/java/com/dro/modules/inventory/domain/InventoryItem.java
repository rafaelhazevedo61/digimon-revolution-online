package com.dro.modules.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public UUID getId () {
        return id;
    }

    public void setId (UUID id) {
        this.id = id;
    }

    public UUID getDigimonId () {
        return digimonId;
    }

    public void setDigimonId (UUID digimonId) {
        this.digimonId = digimonId;
    }

    public ItemType getItemType () {
        return itemType;
    }

    public void setItemType (ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemDefinition getItemDefinition () {
        return itemDefinition;
    }

    public void setItemDefinition (ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public int getQuantity () {
        return quantity;
    }

    public void setQuantity (int quantity) {
        this.quantity = quantity;
    }
}
