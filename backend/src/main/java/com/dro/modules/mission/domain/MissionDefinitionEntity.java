package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "mission_definitions")
public class MissionDefinitionEntity implements Persistable<String> {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;
    @Enumerated(EnumType.STRING)
    @Column(name = "required_stage", nullable = false)
    private Stage requiredStage;
    @Column(name = "required_level", nullable = false)
    private int requiredLevel;
    @Column(name = "base_xp", nullable = false)
    private int baseXp;
    @Column(name = "base_bits", nullable = false)
    private int baseBits;
    @Column(name = "energy_cost", nullable = false)
    private int energyCost;
    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chest_definition_id")


    private ChestDefinitionEntity chestDefinition;
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MissionRewardEntity> rewards;
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MissionLootChanceEntity> lootChances;
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MissionLootItemEntity> lootItems;
    @Transient
    private boolean newEntity;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Stage getRequiredStage() {
        return requiredStage;
    }

    public void setRequiredStage(Stage requiredStage) {
        this.requiredStage = requiredStage;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getBaseXp() {
        return baseXp;
    }

    public void setBaseXp(int baseXp) {
        this.baseXp = baseXp;
    }

    public int getBaseBits() {
        return baseBits;
    }

    public void setBaseBits(int baseBits) {
        this.baseBits = baseBits;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ChestDefinitionEntity getChestDefinition() {
        return chestDefinition;
    }

    public void setChestDefinition(ChestDefinitionEntity chestDefinition) {
        this.chestDefinition = chestDefinition;
    }

    public List<MissionRewardEntity> getRewards() {
        return rewards;
    }

    public void setRewards(List<MissionRewardEntity> rewards) {
        this.rewards = rewards;
    }

    public List<MissionLootChanceEntity> getLootChances() {
        return lootChances;
    }

    public void setLootChances(List<MissionLootChanceEntity> lootChances) {
        this.lootChances = lootChances;
    }

    public List<MissionLootItemEntity> getLootItems() {
        return lootItems;
    }

    public void setLootItems(List<MissionLootItemEntity> lootItems) {
        this.lootItems = lootItems;
    }

    public boolean isNewEntity() {
        return newEntity;
    }

    public void setNewEntity(boolean newEntity) {
        this.newEntity = newEntity;
    }

    private static int $default$baseBits() {
        return 0;
    }

    private static boolean $default$active() {
        return true;
    }

    private static List<MissionRewardEntity> $default$rewards() {
        return new ArrayList<>();
    }

    private static List<MissionLootChanceEntity> $default$lootChances() {
        return new ArrayList<>();
    }

    private static List<MissionLootItemEntity> $default$lootItems() {
        return new ArrayList<>();
    }

    private static boolean $default$newEntity() {
        return false;
    }


    public static class MissionDefinitionEntityBuilder {
        private String id;
        private String name;
        private String description;
        private Area area;
        private Stage requiredStage;
        private int requiredLevel;
        private int baseXp;
        private boolean baseBits$set;
        private int baseBits$value;
        private int energyCost;
        private int durationSeconds;
        private boolean active$set;
        private boolean active$value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private ChestDefinitionEntity chestDefinition;
        private boolean rewards$set;
        private List<MissionRewardEntity> rewards$value;
        private boolean lootChances$set;
        private List<MissionLootChanceEntity> lootChances$value;
        private boolean lootItems$set;
        private List<MissionLootItemEntity> lootItems$value;
        private boolean newEntity$set;
        private boolean newEntity$value;

        MissionDefinitionEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder area(final Area area) {
            this.area = area;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder requiredStage(final Stage requiredStage) {
            this.requiredStage = requiredStage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder requiredLevel(final int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder baseXp(final int baseXp) {
            this.baseXp = baseXp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder baseBits(final int baseBits) {
            this.baseBits$value = baseBits;
            baseBits$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder energyCost(final int energyCost) {
            this.energyCost = energyCost;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder durationSeconds(final int durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder chestDefinition(final ChestDefinitionEntity chestDefinition) {
            this.chestDefinition = chestDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder rewards(final List<MissionRewardEntity> rewards) {
            this.rewards$value = rewards;
            rewards$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder lootChances(final List<MissionLootChanceEntity> lootChances) {
            this.lootChances$value = lootChances;
            lootChances$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder lootItems(final List<MissionLootItemEntity> lootItems) {
            this.lootItems$value = lootItems;
            lootItems$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionDefinitionEntity.MissionDefinitionEntityBuilder newEntity(final boolean newEntity) {
            this.newEntity$value = newEntity;
            newEntity$set = true;
            return this;
        }

        public MissionDefinitionEntity build() {
            int baseBits$value = this.baseBits$value;
            if (!this.baseBits$set) baseBits$value = MissionDefinitionEntity.$default$baseBits();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = MissionDefinitionEntity.$default$active();
            List<MissionRewardEntity> rewards$value = this.rewards$value;
            if (!this.rewards$set) rewards$value = MissionDefinitionEntity.$default$rewards();
            List<MissionLootChanceEntity> lootChances$value = this.lootChances$value;
            if (!this.lootChances$set) lootChances$value = MissionDefinitionEntity.$default$lootChances();
            List<MissionLootItemEntity> lootItems$value = this.lootItems$value;
            if (!this.lootItems$set) lootItems$value = MissionDefinitionEntity.$default$lootItems();
            boolean newEntity$value = this.newEntity$value;
            if (!this.newEntity$set) newEntity$value = MissionDefinitionEntity.$default$newEntity();
            return new MissionDefinitionEntity(this.id, this.name, this.description, this.area, this.requiredStage, this.requiredLevel, this.baseXp, baseBits$value, this.energyCost, this.durationSeconds, active$value, this.createdAt, this.updatedAt, this.createdBy, this.updatedBy, this.chestDefinition, rewards$value, lootChances$value, lootItems$value, newEntity$value);
        }

        @Override
        public String toString() {
            return "MissionDefinitionEntity.MissionDefinitionEntityBuilder(id=" + this.id + ", name=" + this.name + ", description=" + this.description + ", area=" + this.area + ", requiredStage=" + this.requiredStage + ", requiredLevel=" + this.requiredLevel + ", baseXp=" + this.baseXp + ", baseBits$value=" + this.baseBits$value + ", energyCost=" + this.energyCost + ", durationSeconds=" + this.durationSeconds + ", active$value=" + this.active$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdBy=" + this.createdBy + ", updatedBy=" + this.updatedBy + ", chestDefinition=" + this.chestDefinition + ", rewards$value=" + this.rewards$value + ", lootChances$value=" + this.lootChances$value + ", lootItems$value=" + this.lootItems$value + ", newEntity$value=" + this.newEntity$value + ")";
        }
    }

    public static MissionDefinitionEntity.MissionDefinitionEntityBuilder builder() {
        return new MissionDefinitionEntity.MissionDefinitionEntityBuilder();
    }

    public MissionDefinitionEntity() {
        this.baseBits = MissionDefinitionEntity.$default$baseBits();
        this.active = MissionDefinitionEntity.$default$active();
        this.rewards = MissionDefinitionEntity.$default$rewards();
        this.lootChances = MissionDefinitionEntity.$default$lootChances();
        this.lootItems = MissionDefinitionEntity.$default$lootItems();
        this.newEntity = MissionDefinitionEntity.$default$newEntity();
    }

    public MissionDefinitionEntity(final String id, final String name, final String description, final Area area, final Stage requiredStage, final int requiredLevel, final int baseXp, final int baseBits, final int energyCost, final int durationSeconds, final boolean active, final LocalDateTime createdAt, final LocalDateTime updatedAt, final String createdBy, final String updatedBy, final ChestDefinitionEntity chestDefinition, final List<MissionRewardEntity> rewards, final List<MissionLootChanceEntity> lootChances, final List<MissionLootItemEntity> lootItems, final boolean newEntity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.area = area;
        this.requiredStage = requiredStage;
        this.requiredLevel = requiredLevel;
        this.baseXp = baseXp;
        this.baseBits = baseBits;
        this.energyCost = energyCost;
        this.durationSeconds = durationSeconds;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.chestDefinition = chestDefinition;
        this.rewards = rewards;
        this.lootChances = lootChances;
        this.lootItems = lootItems;
        this.newEntity = newEntity;
    }
}
