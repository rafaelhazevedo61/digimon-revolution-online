package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.ClanUpgradeEffectType;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_upgrade_types")
public class ClanUpgradeType {
    @Id
    @Column(name = "code", length = 30)
    private String code;
    @Column(nullable = false, length = 60)
    private String name;
    @Column(length = 280)
    private String description;
    @Column(name = "unlocked_at_clan_level", nullable = false)
    private int unlockedAtClanLevel;
    @Column(name = "max_level", nullable = false)
    private int maxLevel;
    @Column(name = "base_honor_marks_cost", nullable = false)
    private int baseHonorMarksCost;
    @Column(name = "cost_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal costMultiplier;
    @Column(name = "effect_per_level", nullable = false, precision = 5, scale = 4)
    private BigDecimal effectPerLevel;
    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false, length = 30)
    private ClanUpgradeEffectType effectType;
    @Column(length = 30)
    private String stat;

    private static int $default$maxLevel() {
        return 10;
    }


    public static class ClanUpgradeTypeBuilder {
        private String code;
        private String name;
        private String description;
        private int unlockedAtClanLevel;
        private boolean maxLevel$set;
        private int maxLevel$value;
        private int baseHonorMarksCost;
        private BigDecimal costMultiplier;
        private BigDecimal effectPerLevel;
        private ClanUpgradeEffectType effectType;
        private String stat;

        ClanUpgradeTypeBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder unlockedAtClanLevel(final int unlockedAtClanLevel) {
            this.unlockedAtClanLevel = unlockedAtClanLevel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder maxLevel(final int maxLevel) {
            this.maxLevel$value = maxLevel;
            maxLevel$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder baseHonorMarksCost(final int baseHonorMarksCost) {
            this.baseHonorMarksCost = baseHonorMarksCost;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder costMultiplier(final BigDecimal costMultiplier) {
            this.costMultiplier = costMultiplier;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder effectPerLevel(final BigDecimal effectPerLevel) {
            this.effectPerLevel = effectPerLevel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder effectType(final ClanUpgradeEffectType effectType) {
            this.effectType = effectType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradeType.ClanUpgradeTypeBuilder stat(final String stat) {
            this.stat = stat;
            return this;
        }

        public ClanUpgradeType build() {
            int maxLevel$value = this.maxLevel$value;
            if (!this.maxLevel$set) maxLevel$value = ClanUpgradeType.$default$maxLevel();
            return new ClanUpgradeType(this.code, this.name, this.description, this.unlockedAtClanLevel, maxLevel$value, this.baseHonorMarksCost, this.costMultiplier, this.effectPerLevel, this.effectType, this.stat);
        }

        @Override
        public String toString() {
            return "ClanUpgradeType.ClanUpgradeTypeBuilder(code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", unlockedAtClanLevel=" + this.unlockedAtClanLevel + ", maxLevel$value=" + this.maxLevel$value + ", baseHonorMarksCost=" + this.baseHonorMarksCost + ", costMultiplier=" + this.costMultiplier + ", effectPerLevel=" + this.effectPerLevel + ", effectType=" + this.effectType + ", stat=" + this.stat + ")";
        }
    }

    public static ClanUpgradeType.ClanUpgradeTypeBuilder builder() {
        return new ClanUpgradeType.ClanUpgradeTypeBuilder();
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

    public int getUnlockedAtClanLevel() {
        return this.unlockedAtClanLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getBaseHonorMarksCost() {
        return this.baseHonorMarksCost;
    }

    public BigDecimal getCostMultiplier() {
        return this.costMultiplier;
    }

    public BigDecimal getEffectPerLevel() {
        return this.effectPerLevel;
    }

    public ClanUpgradeEffectType getEffectType() {
        return this.effectType;
    }

    public String getStat() {
        return this.stat;
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

    public void setUnlockedAtClanLevel(final int unlockedAtClanLevel) {
        this.unlockedAtClanLevel = unlockedAtClanLevel;
    }

    public void setMaxLevel(final int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setBaseHonorMarksCost(final int baseHonorMarksCost) {
        this.baseHonorMarksCost = baseHonorMarksCost;
    }

    public void setCostMultiplier(final BigDecimal costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public void setEffectPerLevel(final BigDecimal effectPerLevel) {
        this.effectPerLevel = effectPerLevel;
    }

    public void setEffectType(final ClanUpgradeEffectType effectType) {
        this.effectType = effectType;
    }

    public void setStat(final String stat) {
        this.stat = stat;
    }

    public ClanUpgradeType() {
        this.maxLevel = ClanUpgradeType.$default$maxLevel();
    }

    public ClanUpgradeType(final String code, final String name, final String description, final int unlockedAtClanLevel, final int maxLevel, final int baseHonorMarksCost, final BigDecimal costMultiplier, final BigDecimal effectPerLevel, final ClanUpgradeEffectType effectType, final String stat) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.unlockedAtClanLevel = unlockedAtClanLevel;
        this.maxLevel = maxLevel;
        this.baseHonorMarksCost = baseHonorMarksCost;
        this.costMultiplier = costMultiplier;
        this.effectPerLevel = effectPerLevel;
        this.effectType = effectType;
        this.stat = stat;
    }
}
