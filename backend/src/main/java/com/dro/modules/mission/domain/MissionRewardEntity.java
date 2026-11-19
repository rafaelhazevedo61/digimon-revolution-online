package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "mission_rewards")
public class MissionRewardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private MissionDefinitionEntity mission;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    @Column(name = "base_quantity", nullable = false)
    private int baseQuantity;


    public static class MissionRewardEntityBuilder {
        private Long id;
        private MissionDefinitionEntity mission;
        private ItemType itemType;
        private int baseQuantity;

        MissionRewardEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MissionRewardEntity.MissionRewardEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public MissionRewardEntity.MissionRewardEntityBuilder mission(final MissionDefinitionEntity mission) {
            this.mission = mission;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionRewardEntity.MissionRewardEntityBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MissionRewardEntity.MissionRewardEntityBuilder baseQuantity(final int baseQuantity) {
            this.baseQuantity = baseQuantity;
            return this;
        }

        public MissionRewardEntity build() {
            return new MissionRewardEntity(this.id, this.mission, this.itemType, this.baseQuantity);
        }

        @Override
        public String toString() {
            return "MissionRewardEntity.MissionRewardEntityBuilder(id=" + this.id + ", mission=" + this.mission + ", itemType=" + this.itemType + ", baseQuantity=" + this.baseQuantity + ")";
        }
    }

    public static MissionRewardEntity.MissionRewardEntityBuilder builder() {
        return new MissionRewardEntity.MissionRewardEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public MissionDefinitionEntity getMission() {
        return this.mission;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public int getBaseQuantity() {
        return this.baseQuantity;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setMission(final MissionDefinitionEntity mission) {
        this.mission = mission;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setBaseQuantity(final int baseQuantity) {
        this.baseQuantity = baseQuantity;
    }

    public MissionRewardEntity() {
    }

    public MissionRewardEntity(final Long id, final MissionDefinitionEntity mission, final ItemType itemType, final int baseQuantity) {
        this.id = id;
        this.mission = mission;
        this.itemType = itemType;
        this.baseQuantity = baseQuantity;
    }
}
