package com.dro.modules.loot.domain;

import jakarta.persistence.*;

/**
 * Peso de uma raridade dentro de uma loot table.
 */
@Entity
@Table(name = "loot_table_rarity_weights", uniqueConstraints = @UniqueConstraint(columnNames = {"loot_table_id", "rarity"}))
public class LootTableRarityWeightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_table_id", nullable = false)


    private LootTableEntity lootTable;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;
    @Column(nullable = false)
    private int weight;


    public static class LootTableRarityWeightEntityBuilder {
        private Long id;
        private LootTableEntity lootTable;
        private LootRarity rarity;
        private int weight;

        LootTableRarityWeightEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder lootTable(final LootTableEntity lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder weight(final int weight) {
            this.weight = weight;
            return this;
        }

        public LootTableRarityWeightEntity build() {
            return new LootTableRarityWeightEntity(this.id, this.lootTable, this.rarity, this.weight);
        }

        @Override
        public String toString() {
            return "LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder(id=" + this.id + ", lootTable=" + this.lootTable + ", rarity=" + this.rarity + ", weight=" + this.weight + ")";
        }
    }

    public static LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder builder() {
        return new LootTableRarityWeightEntity.LootTableRarityWeightEntityBuilder();
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

    public int getWeight() {
        return this.weight;
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

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public LootTableRarityWeightEntity() {
    }

    public LootTableRarityWeightEntity(final Long id, final LootTableEntity lootTable, final LootRarity rarity, final int weight) {
        this.id = id;
        this.lootTable = lootTable;
        this.rarity = rarity;
        this.weight = weight;
    }
}
