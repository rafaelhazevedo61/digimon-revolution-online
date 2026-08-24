package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;

/**
 * Entrada ponderada de uma loot table.
 *
 * <p>Materiais nomeados usam {@link ItemType#EVOLUTION_MATERIAL} e o código do
 * material. Baús usam {@link ItemType#LOOT_CHEST} e o código do baú. Os demais
 * itens são identificados diretamente pelo {@link ItemType}.</p>
 */
@Entity
@Table(name = "loot_table_entries")
public class LootTableEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_table_id", nullable = false)


    private LootTableEntity lootTable;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;
    @Column(name = "material_code", length = 80)
    private String materialCode;
    @Column(nullable = false)
    private int weight;
    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;
    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;
    @Column(nullable = false)
    private boolean active;

    private static boolean $default$active() {
        return true;
    }


    public static class LootTableEntryEntityBuilder {
        private Long id;
        private LootTableEntity lootTable;
        private LootRarity rarity;
        private ItemType itemType;
        private String materialCode;
        private int weight;
        private int minQuantity;
        private int maxQuantity;
        private boolean active$set;
        private boolean active$value;

        LootTableEntryEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder lootTable(final LootTableEntity lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder materialCode(final String materialCode) {
            this.materialCode = materialCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder weight(final int weight) {
            this.weight = weight;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder minQuantity(final int minQuantity) {
            this.minQuantity = minQuantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder maxQuantity(final int maxQuantity) {
            this.maxQuantity = maxQuantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntryEntity.LootTableEntryEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        public LootTableEntryEntity build() {
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = LootTableEntryEntity.$default$active();
            return new LootTableEntryEntity(this.id, this.lootTable, this.rarity, this.itemType, this.materialCode, this.weight, this.minQuantity, this.maxQuantity, active$value);
        }

        @Override
        public String toString() {
            return "LootTableEntryEntity.LootTableEntryEntityBuilder(id=" + this.id + ", lootTable=" + this.lootTable + ", rarity=" + this.rarity + ", itemType=" + this.itemType + ", materialCode=" + this.materialCode + ", weight=" + this.weight + ", minQuantity=" + this.minQuantity + ", maxQuantity=" + this.maxQuantity + ", active$value=" + this.active$value + ")";
        }
    }

    public static LootTableEntryEntity.LootTableEntryEntityBuilder builder() {
        return new LootTableEntryEntity.LootTableEntryEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public LootTableEntity getLootTable() {
        return this.lootTable;
    }

    public LootRarity getRarity() {
        return this.rarity;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public String getMaterialCode() {
        return this.materialCode;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMinQuantity() {
        return this.minQuantity;
    }

    public int getMaxQuantity() {
        return this.maxQuantity;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setLootTable(final LootTableEntity lootTable) {
        this.lootTable = lootTable;
    }

    public void setRarity(final LootRarity rarity) {
        this.rarity = rarity;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setMaterialCode(final String materialCode) {
        this.materialCode = materialCode;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public void setMinQuantity(final int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public void setMaxQuantity(final int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public LootTableEntryEntity() {
        this.active = LootTableEntryEntity.$default$active();
    }

    public LootTableEntryEntity(final Long id, final LootTableEntity lootTable, final LootRarity rarity, final ItemType itemType, final String materialCode, final int weight, final int minQuantity, final int maxQuantity, final boolean active) {
        this.id = id;
        this.lootTable = lootTable;
        this.rarity = rarity;
        this.itemType = itemType;
        this.materialCode = materialCode;
        this.weight = weight;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.active = active;
    }
}
