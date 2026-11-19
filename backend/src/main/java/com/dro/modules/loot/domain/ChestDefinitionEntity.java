package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Definição de um baú temático do jogo.
 *
 * <p>O código do baú é também o código da {@link ItemDefinition} usada pelo
 * inventário para diferenciar baús negociáveis de origens distintas.</p>
 */
@Entity
@Table(name = "chest_definitions")
public class ChestDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 120)
    private String icon;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_table_id", nullable = false)


    private LootTableEntity lootTable;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_definition_id", nullable = false, unique = true)


    private ItemDefinition itemDefinition;
    @Column(nullable = false)
    private boolean tradable;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "created_by", nullable = false, updatable = false, length = 80)
    private String createdBy;
    @Column(name = "updated_by", nullable = false, length = 80)
    private String updatedBy;

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

    private static boolean $default$tradable() {
        return true;
    }

    private static boolean $default$active() {
        return true;
    }

    private static String $default$createdBy() {
        return "SYSTEM";
    }

    private static String $default$updatedBy() {
        return "SYSTEM";
    }


    public static class ChestDefinitionEntityBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private String icon;
        private LootTableEntity lootTable;
        private ItemDefinition itemDefinition;
        private boolean tradable$set;
        private boolean tradable$value;
        private boolean active$set;
        private boolean active$value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean createdBy$set;
        private String createdBy$value;
        private boolean updatedBy$set;
        private String updatedBy$value;

        ChestDefinitionEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder icon(final String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder lootTable(final LootTableEntity lootTable) {
            this.lootTable = lootTable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder itemDefinition(final ItemDefinition itemDefinition) {
            this.itemDefinition = itemDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder tradable(final boolean tradable) {
            this.tradable$value = tradable;
            tradable$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder createdBy(final String createdBy) {
            this.createdBy$value = createdBy;
            createdBy$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestDefinitionEntity.ChestDefinitionEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy$value = updatedBy;
            updatedBy$set = true;
            return this;
        }

        public ChestDefinitionEntity build() {
            boolean tradable$value = this.tradable$value;
            if (!this.tradable$set) tradable$value = ChestDefinitionEntity.$default$tradable();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = ChestDefinitionEntity.$default$active();
            String createdBy$value = this.createdBy$value;
            if (!this.createdBy$set) createdBy$value = ChestDefinitionEntity.$default$createdBy();
            String updatedBy$value = this.updatedBy$value;
            if (!this.updatedBy$set) updatedBy$value = ChestDefinitionEntity.$default$updatedBy();
            return new ChestDefinitionEntity(this.id, this.code, this.name, this.description, this.icon, this.lootTable, this.itemDefinition, tradable$value, active$value, this.createdAt, this.updatedAt, createdBy$value, updatedBy$value);
        }

        @Override
        public String toString() {
            return "ChestDefinitionEntity.ChestDefinitionEntityBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", icon=" + this.icon + ", lootTable=" + this.lootTable + ", itemDefinition=" + this.itemDefinition + ", tradable$value=" + this.tradable$value + ", active$value=" + this.active$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdBy$value=" + this.createdBy$value + ", updatedBy$value=" + this.updatedBy$value + ")";
        }
    }

    public static ChestDefinitionEntity.ChestDefinitionEntityBuilder builder() {
        return new ChestDefinitionEntity.ChestDefinitionEntityBuilder();
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

    public String getIcon() {
        return this.icon;
    }

    public LootTableEntity getLootTable() {
        return this.lootTable;
    }

    public ItemDefinition getItemDefinition() {
        return this.itemDefinition;
    }

    public boolean isTradable() {
        return this.tradable;
    }

    public boolean isActive() {
        return this.active;
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

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public void setLootTable(final LootTableEntity lootTable) {
        this.lootTable = lootTable;
    }

    public void setItemDefinition(final ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public void setTradable(final boolean tradable) {
        this.tradable = tradable;
    }

    public void setActive(final boolean active) {
        this.active = active;
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

    public ChestDefinitionEntity() {
        this.tradable = ChestDefinitionEntity.$default$tradable();
        this.active = ChestDefinitionEntity.$default$active();
        this.createdBy = ChestDefinitionEntity.$default$createdBy();
        this.updatedBy = ChestDefinitionEntity.$default$updatedBy();
    }

    public ChestDefinitionEntity(final Long id, final String code, final String name, final String description, final String icon, final LootTableEntity lootTable, final ItemDefinition itemDefinition, final boolean tradable, final boolean active, final LocalDateTime createdAt, final LocalDateTime updatedAt, final String createdBy, final String updatedBy) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.lootTable = lootTable;
        this.itemDefinition = itemDefinition;
        this.tradable = tradable;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
