package com.dro.modules.mission.domain;

import com.dro.modules.loot.domain.LootRarity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "mission_loot_chances")
public class MissionLootChanceEntity {
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
    @Column(nullable = false)
    private int chance;


    public static class MissionLootChanceEntityBuilder {
        private Long id;
        private MissionDefinitionEntity mission;
        private LootRarity rarity;
        private int chance;

        MissionLootChanceEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MissionLootChanceEntity.MissionLootChanceEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public MissionLootChanceEntity.MissionLootChanceEntityBuilder mission(final MissionDefinitionEntity mission) {
            this.mission = mission;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionLootChanceEntity.MissionLootChanceEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionLootChanceEntity.MissionLootChanceEntityBuilder chance(final int chance) {
            this.chance = chance;
            return this;
        }

        public MissionLootChanceEntity build() {
            return new MissionLootChanceEntity(this.id, this.mission, this.rarity, this.chance);
        }

        @Override
        public String toString() {
            return "MissionLootChanceEntity.MissionLootChanceEntityBuilder(id=" + this.id + ", mission=" + this.mission + ", rarity=" + this.rarity + ", chance=" + this.chance + ")";
        }
    }

    public static MissionLootChanceEntity.MissionLootChanceEntityBuilder builder() {
        return new MissionLootChanceEntity.MissionLootChanceEntityBuilder();
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

    public int getChance() {
        return this.chance;
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

    public void setChance(final int chance) {
        this.chance = chance;
    }

    public MissionLootChanceEntity() {
    }

    public MissionLootChanceEntity(final Long id, final MissionDefinitionEntity mission, final LootRarity rarity, final int chance) {
        this.id = id;
        this.mission = mission;
        this.rarity = rarity;
        this.chance = chance;
    }
}
