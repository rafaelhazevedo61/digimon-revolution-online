package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import jakarta.persistence.*;
import lombok.*;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private int baseBits = 0;

    @Column(name = "energy_cost", nullable = false)
    private int energyCost;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChestDefinitionEntity chestDefinition;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionRewardEntity> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootChanceEntity> lootChances = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootItemEntity> lootItems = new ArrayList<>();

    @Transient
    @Builder.Default
    private boolean newEntity = false;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @Nullable
    @Override
    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public Area getArea () {
        return area;
    }

    public void setArea (Area area) {
        this.area = area;
    }

    public Stage getRequiredStage () {
        return requiredStage;
    }

    public void setRequiredStage (Stage requiredStage) {
        this.requiredStage = requiredStage;
    }

    public int getRequiredLevel () {
        return requiredLevel;
    }

    public void setRequiredLevel (int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getBaseXp () {
        return baseXp;
    }

    public void setBaseXp (int baseXp) {
        this.baseXp = baseXp;
    }

    public int getBaseBits () {
        return baseBits;
    }

    public void setBaseBits (int baseBits) {
        this.baseBits = baseBits;
    }

    public int getEnergyCost () {
        return energyCost;
    }

    public void setEnergyCost (int energyCost) {
        this.energyCost = energyCost;
    }

    public int getDurationSeconds () {
        return durationSeconds;
    }

    public void setDurationSeconds (int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt () {
        return updatedAt;
    }

    public void setUpdatedAt (LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy () {
        return createdBy;
    }

    public void setCreatedBy (String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy () {
        return updatedBy;
    }

    public void setUpdatedBy (String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ChestDefinitionEntity getChestDefinition () {
        return chestDefinition;
    }

    public void setChestDefinition (ChestDefinitionEntity chestDefinition) {
        this.chestDefinition = chestDefinition;
    }

    public List<MissionRewardEntity> getRewards () {
        return rewards;
    }

    public void setRewards (List<MissionRewardEntity> rewards) {
        this.rewards = rewards;
    }

    public List<MissionLootChanceEntity> getLootChances () {
        return lootChances;
    }

    public void setLootChances (List<MissionLootChanceEntity> lootChances) {
        this.lootChances = lootChances;
    }

    public List<MissionLootItemEntity> getLootItems () {
        return lootItems;
    }

    public void setLootItems (List<MissionLootItemEntity> lootItems) {
        this.lootItems = lootItems;
    }

    public boolean isNewEntity () {
        return newEntity;
    }

    public void setNewEntity (boolean newEntity) {
        this.newEntity = newEntity;
    }
}
