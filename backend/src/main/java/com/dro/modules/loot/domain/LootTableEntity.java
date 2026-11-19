package com.dro.modules.loot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo nomeado e reutilizável de pesos de raridade e entradas de loot.
 *
 * <p>A entidade representa somente a configuração persistida. O sorteio e a
 * entrega transacional dos itens serão implementados no Sprint 2.</p>
 */
@Entity
@Table(name = "loot_tables")
public class LootTableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "min_items", nullable = false)
    private int minItems;
    @Column(name = "max_items", nullable = false)
    private int maxItems;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "created_by", nullable = false, updatable = false, length = 80)
    private String createdBy;
    @Column(name = "updated_by", nullable = false, length = 80)
    private String updatedBy;
    @OneToMany(mappedBy = "lootTable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LootTableRarityWeightEntity> rarityWeights;
    @OneToMany(mappedBy = "lootTable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LootTableEntryEntity> entries;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (createdBy == null) {
            createdBy = "SYSTEM";
        }
        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private static boolean $default$active() {
        return true;
    }

    private static int $default$minItems() {
        return 1;
    }

    private static int $default$maxItems() {
        return 4;
    }

    private static String $default$createdBy() {
        return "SYSTEM";
    }

    private static String $default$updatedBy() {
        return "SYSTEM";
    }

    private static List<LootTableRarityWeightEntity> $default$rarityWeights() {
        return new ArrayList<>();
    }

    private static List<LootTableEntryEntity> $default$entries() {
        return new ArrayList<>();
    }


    public static class LootTableEntityBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private boolean active$set;
        private boolean active$value;
        private boolean minItems$set;
        private int minItems$value;
        private boolean maxItems$set;
        private int maxItems$value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean createdBy$set;
        private String createdBy$value;
        private boolean updatedBy$set;
        private String updatedBy$value;
        private boolean rarityWeights$set;
        private List<LootTableRarityWeightEntity> rarityWeights$value;
        private boolean entries$set;
        private List<LootTableEntryEntity> entries$value;

        LootTableEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder minItems(final int minItems) {
            this.minItems$value = minItems;
            minItems$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder maxItems(final int maxItems) {
            this.maxItems$value = maxItems;
            maxItems$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder createdBy(final String createdBy) {
            this.createdBy$value = createdBy;
            createdBy$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy$value = updatedBy;
            updatedBy$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder rarityWeights(final List<LootTableRarityWeightEntity> rarityWeights) {
            this.rarityWeights$value = rarityWeights;
            rarityWeights$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LootTableEntity.LootTableEntityBuilder entries(final List<LootTableEntryEntity> entries) {
            this.entries$value = entries;
            entries$set = true;
            return this;
        }

        public LootTableEntity build() {
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = LootTableEntity.$default$active();
            int minItems$value = this.minItems$value;
            if (!this.minItems$set) minItems$value = LootTableEntity.$default$minItems();
            int maxItems$value = this.maxItems$value;
            if (!this.maxItems$set) maxItems$value = LootTableEntity.$default$maxItems();
            String createdBy$value = this.createdBy$value;
            if (!this.createdBy$set) createdBy$value = LootTableEntity.$default$createdBy();
            String updatedBy$value = this.updatedBy$value;
            if (!this.updatedBy$set) updatedBy$value = LootTableEntity.$default$updatedBy();
            List<LootTableRarityWeightEntity> rarityWeights$value = this.rarityWeights$value;
            if (!this.rarityWeights$set) rarityWeights$value = LootTableEntity.$default$rarityWeights();
            List<LootTableEntryEntity> entries$value = this.entries$value;
            if (!this.entries$set) entries$value = LootTableEntity.$default$entries();
            return new LootTableEntity(this.id, this.code, this.name, this.description, active$value, minItems$value, maxItems$value, this.createdAt, this.updatedAt, createdBy$value, updatedBy$value, rarityWeights$value, entries$value);
        }

        @Override
        public String toString() {
            return "LootTableEntity.LootTableEntityBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", active$value=" + this.active$value + ", minItems$value=" + this.minItems$value + ", maxItems$value=" + this.maxItems$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdBy$value=" + this.createdBy$value + ", updatedBy$value=" + this.updatedBy$value + ", rarityWeights$value=" + this.rarityWeights$value + ", entries$value=" + this.entries$value + ")";
        }
    }

    public static LootTableEntity.LootTableEntityBuilder builder() {
        return new LootTableEntity.LootTableEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getMinItems() {
        return this.minItems;
    }

    public int getMaxItems() {
        return this.maxItems;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public List<LootTableRarityWeightEntity> getRarityWeights() {
        return this.rarityWeights;
    }

    public List<LootTableEntryEntity> getEntries() {
        return this.entries;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setMinItems(final int minItems) {
        this.minItems = minItems;
    }

    public void setMaxItems(final int maxItems) {
        this.maxItems = maxItems;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setRarityWeights(final List<LootTableRarityWeightEntity> rarityWeights) {
        this.rarityWeights = rarityWeights;
    }

    public void setEntries(final List<LootTableEntryEntity> entries) {
        this.entries = entries;
    }

    public LootTableEntity() {
        this.active = LootTableEntity.$default$active();
        this.minItems = LootTableEntity.$default$minItems();
        this.maxItems = LootTableEntity.$default$maxItems();
        this.createdBy = LootTableEntity.$default$createdBy();
        this.updatedBy = LootTableEntity.$default$updatedBy();
        this.rarityWeights = LootTableEntity.$default$rarityWeights();
        this.entries = LootTableEntity.$default$entries();
    }

    public LootTableEntity(final Long id, final String code, final String name, final String description, final boolean active, final int minItems, final int maxItems, final LocalDateTime createdAt, final LocalDateTime updatedAt, final String createdBy, final String updatedBy, final List<LootTableRarityWeightEntity> rarityWeights, final List<LootTableEntryEntity> entries) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.rarityWeights = rarityWeights;
        this.entries = entries;
    }
}
