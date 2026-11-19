package com.dro.modules.boss.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "boss_drops")
public class BossDropEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id", nullable = false)
    @JsonIgnore
    private BossDefinitionEntity boss;
    @Column(name = "drop_type", nullable = false)
    private String dropType;
    @Column(name = "item_code")
    private String itemCode;
    @Column(name = "template_name")
    private String templateName;
    @Column(name = "equipment_rarity")
    private String equipmentRarity;
    @Column(nullable = false)
    private int chance;
    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;
    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;


    public static class BossDropEntityBuilder {
        private Long id;
        private BossDefinitionEntity boss;
        private String dropType;
        private String itemCode;
        private String templateName;
        private String equipmentRarity;
        private int chance;
        private int minQuantity;
        private int maxQuantity;

        BossDropEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public BossDropEntity.BossDropEntityBuilder boss(final BossDefinitionEntity boss) {
            this.boss = boss;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder dropType(final String dropType) {
            this.dropType = dropType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder itemCode(final String itemCode) {
            this.itemCode = itemCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder templateName(final String templateName) {
            this.templateName = templateName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder equipmentRarity(final String equipmentRarity) {
            this.equipmentRarity = equipmentRarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder chance(final int chance) {
            this.chance = chance;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder minQuantity(final int minQuantity) {
            this.minQuantity = minQuantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDropEntity.BossDropEntityBuilder maxQuantity(final int maxQuantity) {
            this.maxQuantity = maxQuantity;
            return this;
        }

        public BossDropEntity build() {
            return new BossDropEntity(this.id, this.boss, this.dropType, this.itemCode, this.templateName, this.equipmentRarity, this.chance, this.minQuantity, this.maxQuantity);
        }

        @Override
        public String toString() {
            return "BossDropEntity.BossDropEntityBuilder(id=" + this.id + ", boss=" + this.boss + ", dropType=" + this.dropType + ", itemCode=" + this.itemCode + ", templateName=" + this.templateName + ", equipmentRarity=" + this.equipmentRarity + ", chance=" + this.chance + ", minQuantity=" + this.minQuantity + ", maxQuantity=" + this.maxQuantity + ")";
        }
    }

    public static BossDropEntity.BossDropEntityBuilder builder() {
        return new BossDropEntity.BossDropEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public BossDefinitionEntity getBoss() {
        return this.boss;
    }

    public String getDropType() {
        return this.dropType;
    }

    public String getItemCode() {
        return this.itemCode;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public String getEquipmentRarity() {
        return this.equipmentRarity;
    }

    public int getChance() {
        return this.chance;
    }

    public int getMinQuantity() {
        return this.minQuantity;
    }

    public int getMaxQuantity() {
        return this.maxQuantity;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setBoss(final BossDefinitionEntity boss) {
        this.boss = boss;
    }

    public void setDropType(final String dropType) {
        this.dropType = dropType;
    }

    public void setItemCode(final String itemCode) {
        this.itemCode = itemCode;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public void setEquipmentRarity(final String equipmentRarity) {
        this.equipmentRarity = equipmentRarity;
    }

    public void setChance(final int chance) {
        this.chance = chance;
    }

    public void setMinQuantity(final int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public void setMaxQuantity(final int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public BossDropEntity() {
    }

    public BossDropEntity(final Long id, final BossDefinitionEntity boss, final String dropType, final String itemCode, final String templateName, final String equipmentRarity, final int chance, final int minQuantity, final int maxQuantity) {
        this.id = id;
        this.boss = boss;
        this.dropType = dropType;
        this.itemCode = itemCode;
        this.templateName = templateName;
        this.equipmentRarity = equipmentRarity;
        this.chance = chance;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }
}
