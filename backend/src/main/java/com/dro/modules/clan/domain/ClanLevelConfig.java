package com.dro.modules.clan.domain;

import jakarta.persistence.*;

/**
 * Componente da camada de configuração compartilhada do módulo de Clãs.
 */
@Entity
@Table(name = "clan_level_config")
public class ClanLevelConfig {
    @Id
    private int level;
    @Column(name = "xp_required", nullable = false)
    private int xpRequired;
    @Column(name = "max_members_bonus", nullable = false)
    private int maxMembersBonus;


    public static class ClanLevelConfigBuilder {
        private int level;
        private int xpRequired;
        private int maxMembersBonus;

        ClanLevelConfigBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanLevelConfig.ClanLevelConfigBuilder level(final int level) {
            this.level = level;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanLevelConfig.ClanLevelConfigBuilder xpRequired(final int xpRequired) {
            this.xpRequired = xpRequired;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanLevelConfig.ClanLevelConfigBuilder maxMembersBonus(final int maxMembersBonus) {
            this.maxMembersBonus = maxMembersBonus;
            return this;
        }

        public ClanLevelConfig build() {
            return new ClanLevelConfig(this.level, this.xpRequired, this.maxMembersBonus);
        }

        @Override
        public String toString() {
            return "ClanLevelConfig.ClanLevelConfigBuilder(level=" + this.level + ", xpRequired=" + this.xpRequired + ", maxMembersBonus=" + this.maxMembersBonus + ")";
        }
    }

    public static ClanLevelConfig.ClanLevelConfigBuilder builder() {
        return new ClanLevelConfig.ClanLevelConfigBuilder();
    }

    public int getLevel() {
        return this.level;
    }

    public int getXpRequired() {
        return this.xpRequired;
    }

    public int getMaxMembersBonus() {
        return this.maxMembersBonus;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public void setXpRequired(final int xpRequired) {
        this.xpRequired = xpRequired;
    }

    public void setMaxMembersBonus(final int maxMembersBonus) {
        this.maxMembersBonus = maxMembersBonus;
    }

    public ClanLevelConfig() {
    }

    public ClanLevelConfig(final int level, final int xpRequired, final int maxMembersBonus) {
        this.level = level;
        this.xpRequired = xpRequired;
        this.maxMembersBonus = maxMembersBonus;
    }
}
