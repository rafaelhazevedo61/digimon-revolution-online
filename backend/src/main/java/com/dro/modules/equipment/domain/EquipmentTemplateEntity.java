package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;

/**
 * Componente da camada de modelo de domínio do módulo de Equipamentos.
 */
@Entity
@Table(name = "equipment_templates")
public class EquipmentTemplateEntity implements Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "set_code", nullable = false)
    private String setCode;
    @Column(nullable = false)
    private int tier;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;
    @Enumerated(EnumType.STRING)
    @Column
    private EquipmentRarity rarity;
    @Column(name = "bonus_hp", nullable = false)
    private int bonusHp;
    @Column(name = "bonus_attack", nullable = false)
    private int bonusAttack;
    @Column(name = "bonus_defense", nullable = false)
    private int bonusDefense;
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
    @Transient
    private boolean newEntity;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    private static int $default$bonusHp() {
        return 0;
    }

    private static int $default$bonusAttack() {
        return 0;
    }

    private static int $default$bonusDefense() {
        return 0;
    }

    private static boolean $default$active() {
        return true;
    }

    private static boolean $default$newEntity() {
        return false;
    }


    public static class EquipmentTemplateEntityBuilder {
        private Long id;
        private String name;
        private String setCode;
        private int tier;
        private EquipmentSlot slot;
        private EquipmentRarity rarity;
        private boolean bonusHp$set;
        private int bonusHp$value;
        private boolean bonusAttack$set;
        private int bonusAttack$value;
        private boolean bonusDefense$set;
        private int bonusDefense$value;
        private boolean active$set;
        private boolean active$value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private boolean newEntity$set;
        private boolean newEntity$value;

        EquipmentTemplateEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder setCode(final String setCode) {
            this.setCode = setCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder tier(final int tier) {
            this.tier = tier;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder slot(final EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder rarity(final EquipmentRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder bonusHp(final int bonusHp) {
            this.bonusHp$value = bonusHp;
            bonusHp$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder bonusAttack(final int bonusAttack) {
            this.bonusAttack$value = bonusAttack;
            bonusAttack$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder bonusDefense(final int bonusDefense) {
            this.bonusDefense$value = bonusDefense;
            bonusDefense$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentTemplateEntity.EquipmentTemplateEntityBuilder newEntity(final boolean newEntity) {
            this.newEntity$value = newEntity;
            newEntity$set = true;
            return this;
        }

        public EquipmentTemplateEntity build() {
            int bonusHp$value = this.bonusHp$value;
            if (!this.bonusHp$set) bonusHp$value = EquipmentTemplateEntity.$default$bonusHp();
            int bonusAttack$value = this.bonusAttack$value;
            if (!this.bonusAttack$set) bonusAttack$value = EquipmentTemplateEntity.$default$bonusAttack();
            int bonusDefense$value = this.bonusDefense$value;
            if (!this.bonusDefense$set) bonusDefense$value = EquipmentTemplateEntity.$default$bonusDefense();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = EquipmentTemplateEntity.$default$active();
            boolean newEntity$value = this.newEntity$value;
            if (!this.newEntity$set) newEntity$value = EquipmentTemplateEntity.$default$newEntity();
            return new EquipmentTemplateEntity(this.id, this.name, this.setCode, this.tier, this.slot, this.rarity, bonusHp$value, bonusAttack$value, bonusDefense$value, active$value, this.createdAt, this.updatedAt, this.createdBy, this.updatedBy, newEntity$value);
        }

        @Override
        public String toString() {
            return "EquipmentTemplateEntity.EquipmentTemplateEntityBuilder(id=" + this.id + ", name=" + this.name + ", setCode=" + this.setCode + ", tier=" + this.tier + ", slot=" + this.slot + ", rarity=" + this.rarity + ", bonusHp$value=" + this.bonusHp$value + ", bonusAttack$value=" + this.bonusAttack$value + ", bonusDefense$value=" + this.bonusDefense$value + ", active$value=" + this.active$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdBy=" + this.createdBy + ", updatedBy=" + this.updatedBy + ", newEntity$value=" + this.newEntity$value + ")";
        }
    }

    public static EquipmentTemplateEntity.EquipmentTemplateEntityBuilder builder() {
        return new EquipmentTemplateEntity.EquipmentTemplateEntityBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getSetCode() {
        return this.setCode;
    }

    public int getTier() {
        return this.tier;
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

    public boolean isNewEntity() {
        return this.newEntity;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSetCode(final String setCode) {
        this.setCode = setCode;
    }

    public void setTier(final int tier) {
        this.tier = tier;
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

    public void setNewEntity(final boolean newEntity) {
        this.newEntity = newEntity;
    }

    public EquipmentTemplateEntity() {
        this.bonusHp = EquipmentTemplateEntity.$default$bonusHp();
        this.bonusAttack = EquipmentTemplateEntity.$default$bonusAttack();
        this.bonusDefense = EquipmentTemplateEntity.$default$bonusDefense();
        this.active = EquipmentTemplateEntity.$default$active();
        this.newEntity = EquipmentTemplateEntity.$default$newEntity();
    }

    public EquipmentTemplateEntity(final Long id, final String name, final String setCode, final int tier, final EquipmentSlot slot, final EquipmentRarity rarity, final int bonusHp, final int bonusAttack, final int bonusDefense, final boolean active, final LocalDateTime createdAt, final LocalDateTime updatedAt, final String createdBy, final String updatedBy, final boolean newEntity) {
        this.id = id;
        this.name = name;
        this.setCode = setCode;
        this.tier = tier;
        this.slot = slot;
        this.rarity = rarity;
        this.bonusHp = bonusHp;
        this.bonusAttack = bonusAttack;
        this.bonusDefense = bonusDefense;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.newEntity = newEntity;
    }
}
