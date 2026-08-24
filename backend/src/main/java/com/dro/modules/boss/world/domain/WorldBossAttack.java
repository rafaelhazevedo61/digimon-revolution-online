package com.dro.modules.boss.world.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "world_boss_attacks")
public class WorldBossAttack {
    @Id
    private UUID id;
    @Column(name = "world_boss_id", nullable = false)
    private UUID worldBossId;
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
    /**
     * Chave fornecida pelo cliente para tornar o ataque idempotente.
     */
    @Column(name = "request_id", length = 120)
    private String requestId;
    @Column(name = "remaining_hp_after", nullable = false)
    private int remainingHpAfter;
    @Column(name = "win_chance", nullable = false)
    private int winChance;
    @Column(nullable = false)
    private boolean defeated;
    @Column(name = "defeated_reward_xp", nullable = false)
    private int defeatedRewardXp;
    @Column(name = "defeated_reward_bits", nullable = false)
    private int defeatedRewardBits;
    @Column(name = "daily_attacks_remaining", nullable = false)
    private int dailyAttacksRemaining;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public static class WorldBossAttackBuilder {
        private UUID id;
        private UUID worldBossId;
        private UUID playerId;
        private UUID digimonId;
        private int damage;
        private int energyCost;
        private int bitsGained;
        private int xpGained;
        private String requestId;
        private int remainingHpAfter;
        private int winChance;
        private boolean defeated;
        private int defeatedRewardXp;
        private int defeatedRewardBits;
        private int dailyAttacksRemaining;
        private Instant createdAt;

        WorldBossAttackBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder worldBossId(final UUID worldBossId) {
            this.worldBossId = worldBossId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder damage(final int damage) {
            this.damage = damage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder energyCost(final int energyCost) {
            this.energyCost = energyCost;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder bitsGained(final int bitsGained) {
            this.bitsGained = bitsGained;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder xpGained(final int xpGained) {
            this.xpGained = xpGained;
            return this;
        }

        /**
         * Chave fornecida pelo cliente para tornar o ataque idempotente.
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder remainingHpAfter(final int remainingHpAfter) {
            this.remainingHpAfter = remainingHpAfter;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder winChance(final int winChance) {
            this.winChance = winChance;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder defeated(final boolean defeated) {
            this.defeated = defeated;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder defeatedRewardXp(final int defeatedRewardXp) {
            this.defeatedRewardXp = defeatedRewardXp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder defeatedRewardBits(final int defeatedRewardBits) {
            this.defeatedRewardBits = defeatedRewardBits;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder dailyAttacksRemaining(final int dailyAttacksRemaining) {
            this.dailyAttacksRemaining = dailyAttacksRemaining;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossAttack.WorldBossAttackBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorldBossAttack build() {
            return new WorldBossAttack(this.id, this.worldBossId, this.playerId, this.digimonId, this.damage, this.energyCost, this.bitsGained, this.xpGained, this.requestId, this.remainingHpAfter, this.winChance, this.defeated, this.defeatedRewardXp, this.defeatedRewardBits, this.dailyAttacksRemaining, this.createdAt);
        }

        @Override
        public String toString() {
            return "WorldBossAttack.WorldBossAttackBuilder(id=" + this.id + ", worldBossId=" + this.worldBossId + ", playerId=" + this.playerId + ", digimonId=" + this.digimonId + ", damage=" + this.damage + ", energyCost=" + this.energyCost + ", bitsGained=" + this.bitsGained + ", xpGained=" + this.xpGained + ", requestId=" + this.requestId + ", remainingHpAfter=" + this.remainingHpAfter + ", winChance=" + this.winChance + ", defeated=" + this.defeated + ", defeatedRewardXp=" + this.defeatedRewardXp + ", defeatedRewardBits=" + this.defeatedRewardBits + ", dailyAttacksRemaining=" + this.dailyAttacksRemaining + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static WorldBossAttack.WorldBossAttackBuilder builder() {
        return new WorldBossAttack.WorldBossAttackBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getWorldBossId() {
        return this.worldBossId;
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

    /**
     * Chave fornecida pelo cliente para tornar o ataque idempotente.
     */
    public String getRequestId() {
        return this.requestId;
    }

    public int getRemainingHpAfter() {
        return this.remainingHpAfter;
    }

    public int getWinChance() {
        return this.winChance;
    }

    public boolean isDefeated() {
        return this.defeated;
    }

    public int getDefeatedRewardXp() {
        return this.defeatedRewardXp;
    }

    public int getDefeatedRewardBits() {
        return this.defeatedRewardBits;
    }

    public int getDailyAttacksRemaining() {
        return this.dailyAttacksRemaining;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setWorldBossId(final UUID worldBossId) {
        this.worldBossId = worldBossId;
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

    /**
     * Chave fornecida pelo cliente para tornar o ataque idempotente.
     */
    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public void setRemainingHpAfter(final int remainingHpAfter) {
        this.remainingHpAfter = remainingHpAfter;
    }

    public void setWinChance(final int winChance) {
        this.winChance = winChance;
    }

    public void setDefeated(final boolean defeated) {
        this.defeated = defeated;
    }

    public void setDefeatedRewardXp(final int defeatedRewardXp) {
        this.defeatedRewardXp = defeatedRewardXp;
    }

    public void setDefeatedRewardBits(final int defeatedRewardBits) {
        this.defeatedRewardBits = defeatedRewardBits;
    }

    public void setDailyAttacksRemaining(final int dailyAttacksRemaining) {
        this.dailyAttacksRemaining = dailyAttacksRemaining;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public WorldBossAttack() {
    }

    /**
     * Creates a new {@code WorldBossAttack} instance.
     *
     * @param id
     * @param worldBossId
     * @param playerId
     * @param digimonId
     * @param damage
     * @param energyCost
     * @param bitsGained
     * @param xpGained
     * @param requestId Chave fornecida pelo cliente para tornar o ataque idempotente.
     * @param remainingHpAfter
     * @param winChance
     * @param defeated
     * @param defeatedRewardXp
     * @param defeatedRewardBits
     * @param dailyAttacksRemaining
     * @param createdAt
     */
    public WorldBossAttack(final UUID id, final UUID worldBossId, final UUID playerId, final UUID digimonId, final int damage, final int energyCost, final int bitsGained, final int xpGained, final String requestId, final int remainingHpAfter, final int winChance, final boolean defeated, final int defeatedRewardXp, final int defeatedRewardBits, final int dailyAttacksRemaining, final Instant createdAt) {
        this.id = id;
        this.worldBossId = worldBossId;
        this.playerId = playerId;
        this.digimonId = digimonId;
        this.damage = damage;
        this.energyCost = energyCost;
        this.bitsGained = bitsGained;
        this.xpGained = xpGained;
        this.requestId = requestId;
        this.remainingHpAfter = remainingHpAfter;
        this.winChance = winChance;
        this.defeated = defeated;
        this.defeatedRewardXp = defeatedRewardXp;
        this.defeatedRewardBits = defeatedRewardBits;
        this.dailyAttacksRemaining = dailyAttacksRemaining;
        this.createdAt = createdAt;
    }
}
