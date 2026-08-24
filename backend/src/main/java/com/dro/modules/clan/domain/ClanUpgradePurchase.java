package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_upgrade_purchases")
public class ClanUpgradePurchase {
    @Id
    private UUID id;
    @Column(name = "clan_id", nullable = false)
    private UUID clanId;
    @Column(name = "upgrade_code", nullable = false, length = 30)
    private String upgradeCode;
    @Column(nullable = false)
    private int level;
    @Column(name = "total_spent_honor_marks", nullable = false)
    private int totalSpentHonorMarks;

    private static int $default$level() {
        return 0;
    }

    private static int $default$totalSpentHonorMarks() {
        return 0;
    }


    public static class ClanUpgradePurchaseBuilder {
        private UUID id;
        private UUID clanId;
        private String upgradeCode;
        private boolean level$set;
        private int level$value;
        private boolean totalSpentHonorMarks$set;
        private int totalSpentHonorMarks$value;

        ClanUpgradePurchaseBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradePurchase.ClanUpgradePurchaseBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradePurchase.ClanUpgradePurchaseBuilder clanId(final UUID clanId) {
            this.clanId = clanId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradePurchase.ClanUpgradePurchaseBuilder upgradeCode(final String upgradeCode) {
            this.upgradeCode = upgradeCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradePurchase.ClanUpgradePurchaseBuilder level(final int level) {
            this.level$value = level;
            level$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanUpgradePurchase.ClanUpgradePurchaseBuilder totalSpentHonorMarks(final int totalSpentHonorMarks) {
            this.totalSpentHonorMarks$value = totalSpentHonorMarks;
            totalSpentHonorMarks$set = true;
            return this;
        }

        public ClanUpgradePurchase build() {
            int level$value = this.level$value;
            if (!this.level$set) level$value = ClanUpgradePurchase.$default$level();
            int totalSpentHonorMarks$value = this.totalSpentHonorMarks$value;
            if (!this.totalSpentHonorMarks$set) totalSpentHonorMarks$value = ClanUpgradePurchase.$default$totalSpentHonorMarks();
            return new ClanUpgradePurchase(this.id, this.clanId, this.upgradeCode, level$value, totalSpentHonorMarks$value);
        }

        @Override
        public String toString() {
            return "ClanUpgradePurchase.ClanUpgradePurchaseBuilder(id=" + this.id + ", clanId=" + this.clanId + ", upgradeCode=" + this.upgradeCode + ", level$value=" + this.level$value + ", totalSpentHonorMarks$value=" + this.totalSpentHonorMarks$value + ")";
        }
    }

    public static ClanUpgradePurchase.ClanUpgradePurchaseBuilder builder() {
        return new ClanUpgradePurchase.ClanUpgradePurchaseBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getClanId() {
        return this.clanId;
    }

    public String getUpgradeCode() {
        return this.upgradeCode;
    }

    public int getLevel() {
        return this.level;
    }

    public int getTotalSpentHonorMarks() {
        return this.totalSpentHonorMarks;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setClanId(final UUID clanId) {
        this.clanId = clanId;
    }

    public void setUpgradeCode(final String upgradeCode) {
        this.upgradeCode = upgradeCode;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public void setTotalSpentHonorMarks(final int totalSpentHonorMarks) {
        this.totalSpentHonorMarks = totalSpentHonorMarks;
    }

    public ClanUpgradePurchase() {
        this.level = ClanUpgradePurchase.$default$level();
        this.totalSpentHonorMarks = ClanUpgradePurchase.$default$totalSpentHonorMarks();
    }

    public ClanUpgradePurchase(final UUID id, final UUID clanId, final String upgradeCode, final int level, final int totalSpentHonorMarks) {
        this.id = id;
        this.clanId = clanId;
        this.upgradeCode = upgradeCode;
        this.level = level;
        this.totalSpentHonorMarks = totalSpentHonorMarks;
    }
}
