package com.dro.modules.clan.raid.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_raid_attacks")
public class ClanRaidAttack {
    @Id
    private UUID id;
    @Column(name = "clan_raid_id", nullable = false)
    private UUID clanRaidId;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;
    @Column(nullable = false)
    private int damage;
    @Column(name = "energy_cost", nullable = false)
    private int energyCost;
    @Column(name = "bits_gained", nullable = false)
    private int bitsGained;
    @Column(name = "xp_gained", nullable = false)
    private int xpGained;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public static class ClanRaidAttackBuilder {
        private UUID id;
        private UUID clanRaidId;
        private UUID playerId;
        private UUID digimonId;
        private int damage;
        private int energyCost;
        private int bitsGained;
        private int xpGained;
        private Instant createdAt;

        ClanRaidAttackBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder clanRaidId(final UUID clanRaidId) {
            this.clanRaidId = clanRaidId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder damage(final int damage) {
            this.damage = damage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder energyCost(final int energyCost) {
            this.energyCost = energyCost;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder bitsGained(final int bitsGained) {
            this.bitsGained = bitsGained;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder xpGained(final int xpGained) {
            this.xpGained = xpGained;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaidAttack.ClanRaidAttackBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ClanRaidAttack build() {
            return new ClanRaidAttack(this.id, this.clanRaidId, this.playerId, this.digimonId, this.damage, this.energyCost, this.bitsGained, this.xpGained, this.createdAt);
        }

        @Override
        public String toString() {
            return "ClanRaidAttack.ClanRaidAttackBuilder(id=" + this.id + ", clanRaidId=" + this.clanRaidId + ", playerId=" + this.playerId + ", digimonId=" + this.digimonId + ", damage=" + this.damage + ", energyCost=" + this.energyCost + ", bitsGained=" + this.bitsGained + ", xpGained=" + this.xpGained + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static ClanRaidAttack.ClanRaidAttackBuilder builder() {
        return new ClanRaidAttack.ClanRaidAttackBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getClanRaidId() {
        return this.clanRaidId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public UUID getDigimonId() {
        return this.digimonId;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getEnergyCost() {
        return this.energyCost;
    }

    public int getBitsGained() {
        return this.bitsGained;
    }

    public int getXpGained() {
        return this.xpGained;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setClanRaidId(final UUID clanRaidId) {
        this.clanRaidId = clanRaidId;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setDigimonId(final UUID digimonId) {
        this.digimonId = digimonId;
    }

    public void setDamage(final int damage) {
        this.damage = damage;
    }

    public void setEnergyCost(final int energyCost) {
        this.energyCost = energyCost;
    }

    public void setBitsGained(final int bitsGained) {
        this.bitsGained = bitsGained;
    }

    public void setXpGained(final int xpGained) {
        this.xpGained = xpGained;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ClanRaidAttack() {
    }

    public ClanRaidAttack(final UUID id, final UUID clanRaidId, final UUID playerId, final UUID digimonId, final int damage, final int energyCost, final int bitsGained, final int xpGained, final Instant createdAt) {
        this.id = id;
        this.clanRaidId = clanRaidId;
        this.playerId = playerId;
        this.digimonId = digimonId;
        this.damage = damage;
        this.energyCost = energyCost;
        this.bitsGained = bitsGained;
        this.xpGained = xpGained;
        this.createdAt = createdAt;
    }
}
