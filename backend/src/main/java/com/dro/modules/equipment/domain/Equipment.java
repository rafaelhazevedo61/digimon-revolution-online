package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Peça de equipamento individual pertencente a um Digimon.
 *
 * <p>Diferentemente de itens empilháveis, cada equipamento mantém identidade,
 * slot, raridade, tier, set e nível de refinamento próprios. O bônus efetivo
 * combina o valor base com o multiplicador de raridade e o refinamento.</p>
 */
@Entity
@Table(name = "equipments")
public class Equipment {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID playerId;
    @Column
    private UUID digimonId;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentRarity rarity;
    @Column(nullable = false)
    private int bonusHp;
    @Column(nullable = false)
    private int bonusAttack;
    @Column(nullable = false)
    private int bonusDefense;
    @Column(name = "set_code")
    private String setCode;
    @Column
    private int tier;
    @Column(name = "refinement_level", nullable = false)
    private int refinementLevel;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private boolean equipped;

    /**
     * Marca a peça como ocupando seu slot no Digimon.
     */
    public void equip() {
        this.equipped = true;
    }

    /**
     * Libera a peça do slot atualmente ocupado.
     */
    public void unequip() {
        this.equipped = false;
    }

    /**
     * Calcula o bônus efetivo de HP após raridade e refinamento.
     */
    public int getEffectiveBonusHp() {
        if (bonusHp <= 0) return 0;
        return (int) Math.round(bonusHp * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    /**
     * Calcula o bônus efetivo de ATK após raridade e refinamento.
     */
    public int getEffectiveBonusAttack() {
        if (bonusAttack <= 0) return 0;
        return (int) Math.round(bonusAttack * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    /**
     * Calcula o bônus efetivo de DEF após raridade e refinamento.
     */
    public int getEffectiveBonusDefense() {
        if (bonusDefense <= 0) return 0;
        return (int) Math.round(bonusDefense * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    private static int $default$refinementLevel() {
        return 0;
    }

    private static boolean $default$equipped() {
        return false;
    }


    public static class EquipmentBuilder {
        private UUID id;
        private UUID playerId;
        private UUID digimonId;
        private String name;
        private EquipmentSlot slot;
        private EquipmentRarity rarity;
        private int bonusHp;
        private int bonusAttack;
        private int bonusDefense;
        private String setCode;
        private int tier;
        private boolean refinementLevel$set;
        private int refinementLevel$value;
        private LocalDateTime createdAt;
        private boolean equipped$set;
        private boolean equipped$value;

        EquipmentBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Equipment.EquipmentBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder slot(final EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder rarity(final EquipmentRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder bonusHp(final int bonusHp) {
            this.bonusHp = bonusHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder bonusAttack(final int bonusAttack) {
            this.bonusAttack = bonusAttack;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder bonusDefense(final int bonusDefense) {
            this.bonusDefense = bonusDefense;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder setCode(final String setCode) {
            this.setCode = setCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder tier(final int tier) {
            this.tier = tier;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder refinementLevel(final int refinementLevel) {
            this.refinementLevel$value = refinementLevel;
            refinementLevel$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Equipment.EquipmentBuilder equipped(final boolean equipped) {
            this.equipped$value = equipped;
            equipped$set = true;
            return this;
        }

        public Equipment build() {
            int refinementLevel$value = this.refinementLevel$value;
            if (!this.refinementLevel$set) refinementLevel$value = Equipment.$default$refinementLevel();
            boolean equipped$value = this.equipped$value;
            if (!this.equipped$set) equipped$value = Equipment.$default$equipped();
            return new Equipment(this.id, this.playerId, this.digimonId, this.name, this.slot, this.rarity, this.bonusHp, this.bonusAttack, this.bonusDefense, this.setCode, this.tier, refinementLevel$value, this.createdAt, equipped$value);
        }

        @Override
        public String toString() {
            return "Equipment.EquipmentBuilder(id=" + this.id + ", digimonId=" + this.digimonId + ", name=" + this.name + ", slot=" + this.slot + ", rarity=" + this.rarity + ", bonusHp=" + this.bonusHp + ", bonusAttack=" + this.bonusAttack + ", bonusDefense=" + this.bonusDefense + ", setCode=" + this.setCode + ", tier=" + this.tier + ", refinementLevel$value=" + this.refinementLevel$value + ", createdAt=" + this.createdAt + ", equipped$value=" + this.equipped$value + ")";
        }
    }

    public static Equipment.EquipmentBuilder builder() {
        return new Equipment.EquipmentBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getDigimonId() {
        return digimonId;
    }

    public String getName() {
        return this.name;
    }

    public EquipmentSlot getSlot() {
        return this.slot;
    }

    public EquipmentRarity getRarity() {
        return this.rarity;
    }

    public int getBonusHp() {
        return this.bonusHp;
    }

    public int getBonusAttack() {
        return this.bonusAttack;
    }

    public int getBonusDefense() {
        return this.bonusDefense;
    }

    public String getSetCode() {
        return this.setCode;
    }

    public int getTier() {
        return this.tier;
    }

    public int getRefinementLevel() {
        return this.refinementLevel;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public boolean isEquipped() {
        return this.equipped;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setDigimonId(final UUID digimonId) {
        this.digimonId = digimonId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSlot(final EquipmentSlot slot) {
        this.slot = slot;
    }

    public void setRarity(final EquipmentRarity rarity) {
        this.rarity = rarity;
    }

    public void setBonusHp(final int bonusHp) {
        this.bonusHp = bonusHp;
    }

    public void setBonusAttack(final int bonusAttack) {
        this.bonusAttack = bonusAttack;
    }

    public void setBonusDefense(final int bonusDefense) {
        this.bonusDefense = bonusDefense;
    }

    public void setSetCode(final String setCode) {
        this.setCode = setCode;
    }

    public void setTier(final int tier) {
        this.tier = tier;
    }

    public void setRefinementLevel(final int refinementLevel) {
        this.refinementLevel = refinementLevel;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEquipped(final boolean equipped) {
        this.equipped = equipped;
    }

    public Equipment() {
        this.refinementLevel = Equipment.$default$refinementLevel();
        this.equipped = Equipment.$default$equipped();
    }

    public Equipment(final UUID id, final UUID playerId, final UUID digimonId, final String name, final EquipmentSlot slot, final EquipmentRarity rarity, final int bonusHp, final int bonusAttack, final int bonusDefense, final String setCode, final int tier, final int refinementLevel, final LocalDateTime createdAt, final boolean equipped) {
        this.id = id;
        this.playerId = playerId;
        this.digimonId = digimonId;
        this.name = name;
        this.slot = slot;
        this.rarity = rarity;
        this.bonusHp = bonusHp;
        this.bonusAttack = bonusAttack;
        this.bonusDefense = bonusDefense;
        this.setCode = setCode;
        this.tier = tier;
        this.refinementLevel = refinementLevel;
        this.createdAt = createdAt;
        this.equipped = equipped;
    }

    /** Compatibilidade para fixtures antigas que ainda não informam o jogador. */
    public Equipment(final UUID id, final UUID digimonId, final String name, final EquipmentSlot slot, final EquipmentRarity rarity, final int bonusHp, final int bonusAttack, final int bonusDefense, final String setCode, final int tier, final int refinementLevel, final LocalDateTime createdAt, final boolean equipped) {
        this(id, null, digimonId, name, slot, rarity, bonusHp, bonusAttack, bonusDefense, setCode, tier, refinementLevel, createdAt, equipped);
    }
}
