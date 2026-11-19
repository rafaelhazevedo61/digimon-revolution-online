package com.dro.modules.equipment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Configuração persistida das probabilidades de raridade de equipamentos.
 *
 * <p>Os perfis são identificados pelo contexto do sorteio, por exemplo
 * {@code BOSS_NORMAL} ou {@code BOSS_MONTHLY}. A soma dos quatro percentuais
 * é validada no banco e novamente no serviço de aplicação.</p>
 */
@Entity
@Table(name = "equipment_rarity_profiles")
public class EquipmentRarityProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "profile_key", nullable = false, unique = true, length = 40)
    private String profileKey;
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;
    @Column(name = "common_percent", nullable = false)
    private int commonPercent;
    @Column(name = "rare_percent", nullable = false)
    private int rarePercent;
    @Column(name = "epic_percent", nullable = false)
    private int epicPercent;
    @Column(name = "legendary_percent", nullable = false)
    private int legendaryPercent;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "updated_by", nullable = false, length = 80)
    private String updatedBy;

    /**
     * Inicializa os metadados de atualização na criação do perfil.
     */
    @PrePersist
    void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (updatedBy == null) {
            updatedBy = "SYSTEM";
        }
    }

    /**
     * Atualiza o instante da última alteração administrativa.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private static String $default$updatedBy() {
        return "SYSTEM";
    }


    public static class EquipmentRarityProfileEntityBuilder {
        private Long id;
        private String profileKey;
        private String displayName;
        private int commonPercent;
        private int rarePercent;
        private int epicPercent;
        private int legendaryPercent;
        private LocalDateTime updatedAt;
        private boolean updatedBy$set;
        private String updatedBy$value;

        EquipmentRarityProfileEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder profileKey(final String profileKey) {
            this.profileKey = profileKey;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder commonPercent(final int commonPercent) {
            this.commonPercent = commonPercent;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder rarePercent(final int rarePercent) {
            this.rarePercent = rarePercent;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder epicPercent(final int epicPercent) {
            this.epicPercent = epicPercent;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder legendaryPercent(final int legendaryPercent) {
            this.legendaryPercent = legendaryPercent;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy$value = updatedBy;
            updatedBy$set = true;
            return this;
        }

        public EquipmentRarityProfileEntity build() {
            String updatedBy$value = this.updatedBy$value;
            if (!this.updatedBy$set) updatedBy$value = EquipmentRarityProfileEntity.$default$updatedBy();
            return new EquipmentRarityProfileEntity(this.id, this.profileKey, this.displayName, this.commonPercent, this.rarePercent, this.epicPercent, this.legendaryPercent, this.updatedAt, updatedBy$value);
        }

        @Override
        public String toString() {
            return "EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder(id=" + this.id + ", profileKey=" + this.profileKey + ", displayName=" + this.displayName + ", commonPercent=" + this.commonPercent + ", rarePercent=" + this.rarePercent + ", epicPercent=" + this.epicPercent + ", legendaryPercent=" + this.legendaryPercent + ", updatedAt=" + this.updatedAt + ", updatedBy$value=" + this.updatedBy$value + ")";
        }
    }

    public static EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder builder() {
        return new EquipmentRarityProfileEntity.EquipmentRarityProfileEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getProfileKey() {
        return this.profileKey;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getCommonPercent() {
        return this.commonPercent;
    }

    public int getRarePercent() {
        return this.rarePercent;
    }

    public int getEpicPercent() {
        return this.epicPercent;
    }

    public int getLegendaryPercent() {
        return this.legendaryPercent;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setProfileKey(final String profileKey) {
        this.profileKey = profileKey;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setCommonPercent(final int commonPercent) {
        this.commonPercent = commonPercent;
    }

    public void setRarePercent(final int rarePercent) {
        this.rarePercent = rarePercent;
    }

    public void setEpicPercent(final int epicPercent) {
        this.epicPercent = epicPercent;
    }

    public void setLegendaryPercent(final int legendaryPercent) {
        this.legendaryPercent = legendaryPercent;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public EquipmentRarityProfileEntity() {
        this.updatedBy = EquipmentRarityProfileEntity.$default$updatedBy();
    }

    public EquipmentRarityProfileEntity(final Long id, final String profileKey, final String displayName, final int commonPercent, final int rarePercent, final int epicPercent, final int legendaryPercent, final LocalDateTime updatedAt, final String updatedBy) {
        this.id = id;
        this.profileKey = profileKey;
        this.displayName = displayName;
        this.commonPercent = commonPercent;
        this.rarePercent = rarePercent;
        this.epicPercent = epicPercent;
        this.legendaryPercent = legendaryPercent;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}
