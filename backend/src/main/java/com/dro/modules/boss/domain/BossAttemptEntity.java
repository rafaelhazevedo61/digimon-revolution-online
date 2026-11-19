package com.dro.modules.boss.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "boss_attempts")
public class BossAttemptEntity {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;
    @Column(name = "boss_id", nullable = false)
    private Long bossId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BossAttemptStatus status;
    @Column(name = "damage_dealt", nullable = false)
    private int damageDealt;
    @Column(name = "xp_gained", nullable = false)
    private int xpGained;
    @Column(name = "bits_gained", nullable = false)
    private int bitsGained;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public static class BossAttemptEntityBuilder {
        private UUID id;
        private UUID playerId;
        private UUID digimonId;
        private Long bossId;
        private BossAttemptStatus status;
        private int damageDealt;
        private int xpGained;
        private int bitsGained;
        private Instant createdAt;

        BossAttemptEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder bossId(final Long bossId) {
            this.bossId = bossId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder status(final BossAttemptStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder damageDealt(final int damageDealt) {
            this.damageDealt = damageDealt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder xpGained(final int xpGained) {
            this.xpGained = xpGained;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder bitsGained(final int bitsGained) {
            this.bitsGained = bitsGained;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossAttemptEntity.BossAttemptEntityBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BossAttemptEntity build() {
            return new BossAttemptEntity(this.id, this.playerId, this.digimonId, this.bossId, this.status, this.damageDealt, this.xpGained, this.bitsGained, this.createdAt);
        }

        @Override
        public String toString() {
            return "BossAttemptEntity.BossAttemptEntityBuilder(id=" + this.id + ", playerId=" + this.playerId + ", digimonId=" + this.digimonId + ", bossId=" + this.bossId + ", status=" + this.status + ", damageDealt=" + this.damageDealt + ", xpGained=" + this.xpGained + ", bitsGained=" + this.bitsGained + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static BossAttemptEntity.BossAttemptEntityBuilder builder() {
        return new BossAttemptEntity.BossAttemptEntityBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public UUID getDigimonId() {
        return this.digimonId;
    }

    public Long getBossId() {
        return this.bossId;
    }

    public BossAttemptStatus getStatus() {
        return this.status;
    }

    public int getDamageDealt() {
        return this.damageDealt;
    }

    public int getXpGained() {
        return this.xpGained;
    }

    public int getBitsGained() {
        return this.bitsGained;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
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

    public void setBossId(final Long bossId) {
        this.bossId = bossId;
    }

    public void setStatus(final BossAttemptStatus status) {
        this.status = status;
    }

    public void setDamageDealt(final int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public void setXpGained(final int xpGained) {
        this.xpGained = xpGained;
    }

    public void setBitsGained(final int bitsGained) {
        this.bitsGained = bitsGained;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public BossAttemptEntity() {
    }

    public BossAttemptEntity(final UUID id, final UUID playerId, final UUID digimonId, final Long bossId, final BossAttemptStatus status, final int damageDealt, final int xpGained, final int bitsGained, final Instant createdAt) {
        this.id = id;
        this.playerId = playerId;
        this.digimonId = digimonId;
        this.bossId = bossId;
        this.status = status;
        this.damageDealt = damageDealt;
        this.xpGained = xpGained;
        this.bitsGained = bitsGained;
        this.createdAt = createdAt;
    }
}
