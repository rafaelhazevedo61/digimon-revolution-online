package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "mission_loot_items")
public class MissionLootItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private MissionDefinitionEntity mission;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LootRarity rarity;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    @Column(nullable = false)
    private int quantity;


    public static class MissionLootItemEntityBuilder {
        private Long id;
        private MissionDefinitionEntity mission;
        private LootRarity rarity;
        private ItemType itemType;
        private int quantity;

        MissionLootItemEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MissionLootItemEntity.MissionLootItemEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public MissionLootItemEntity.MissionLootItemEntityBuilder mission(final MissionDefinitionEntity mission) {
            this.mission = mission;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionLootItemEntity.MissionLootItemEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionLootItemEntity.MissionLootItemEntityBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionLootItemEntity.MissionLootItemEntityBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        public MissionLootItemEntity build() {
            return new MissionLootItemEntity(this.id, this.mission, this.rarity, this.itemType, this.quantity);
        }

        @Override
        public String toString() {
            return "MissionLootItemEntity.MissionLootItemEntityBuilder(id=" + this.id + ", mission=" + this.mission + ", rarity=" + this.rarity + ", itemType=" + this.itemType + ", quantity=" + this.quantity + ")";
        }
    }

    public static MissionLootItemEntity.MissionLootItemEntityBuilder builder() {
        return new MissionLootItemEntity.MissionLootItemEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public MissionDefinitionEntity getMission() {
        return this.mission;
    }

    public LootRarity getRarity() {
        return this.rarity;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setMission(final MissionDefinitionEntity mission) {
        this.mission = mission;
    }

    public void setRarity(final LootRarity rarity) {
        this.rarity = rarity;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public MissionLootItemEntity() {
    }

    public MissionLootItemEntity(final Long id, final MissionDefinitionEntity mission, final LootRarity rarity, final ItemType itemType, final int quantity) {
        this.id = id;
        this.mission = mission;
        this.rarity = rarity;
        this.itemType = itemType;
        this.quantity = quantity;
    }
}
