package com.dro.modules.boss.world.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "world_boss_instances")
public class WorldBossInstance {
    @Id
    private UUID id;
    @Column(name = "boss_id", nullable = false)
    private Long bossId;
    @Column(name = "boss_date", nullable = false)
    private LocalDate bossDate;
    @Column(name = "cycle_number", nullable = false)
    private int cycleNumber;
    @Column(nullable = false)
    private int maxHp;
    @Column(name = "remaining_hp", nullable = false)
    private int remainingHp;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorldBossStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "defeated_at")
    private Instant defeatedAt;
    @Column(name = "daily_reset_at")
    private Instant dailyResetAt;
    @Version
    private long version;

    private static int $default$cycleNumber() {
        return 1;
    }

    private static long $default$version() {
        return 0;
    }


    public static class WorldBossInstanceBuilder {
        private UUID id;
        private Long bossId;
        private LocalDate bossDate;
        private boolean cycleNumber$set;
        private int cycleNumber$value;
        private int maxHp;
        private int remainingHp;
        private WorldBossStatus status;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant defeatedAt;
        private Instant dailyResetAt;
        private boolean version$set;
        private long version$value;

        WorldBossInstanceBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder bossId(final Long bossId) {
            this.bossId = bossId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder bossDate(final LocalDate bossDate) {
            this.bossDate = bossDate;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder cycleNumber(final int cycleNumber) {
            this.cycleNumber$value = cycleNumber;
            cycleNumber$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder maxHp(final int maxHp) {
            this.maxHp = maxHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder remainingHp(final int remainingHp) {
            this.remainingHp = remainingHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder status(final WorldBossStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder defeatedAt(final Instant defeatedAt) {
            this.defeatedAt = defeatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder dailyResetAt(final Instant dailyResetAt) {
            this.dailyResetAt = dailyResetAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossInstance.WorldBossInstanceBuilder version(final long version) {
            this.version$value = version;
            version$set = true;
            return this;
        }

        public WorldBossInstance build() {
            int cycleNumber$value = this.cycleNumber$value;
            if (!this.cycleNumber$set) cycleNumber$value = WorldBossInstance.$default$cycleNumber();
            long version$value = this.version$value;
            if (!this.version$set) version$value = WorldBossInstance.$default$version();
            return new WorldBossInstance(this.id, this.bossId, this.bossDate, cycleNumber$value, this.maxHp, this.remainingHp, this.status, this.createdAt, this.updatedAt, this.defeatedAt, this.dailyResetAt, version$value);
        }

        @Override
        public String toString() {
            return "WorldBossInstance.WorldBossInstanceBuilder(id=" + this.id + ", bossId=" + this.bossId + ", bossDate=" + this.bossDate + ", cycleNumber$value=" + this.cycleNumber$value + ", maxHp=" + this.maxHp + ", remainingHp=" + this.remainingHp + ", status=" + this.status + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", defeatedAt=" + this.defeatedAt + ", dailyResetAt=" + this.dailyResetAt + ", version$value=" + this.version$value + ")";
        }
    }

    public static WorldBossInstance.WorldBossInstanceBuilder builder() {
        return new WorldBossInstance.WorldBossInstanceBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public Long getBossId() {
        return this.bossId;
    }

    public LocalDate getBossDate() {
        return this.bossDate;
    }

    public int getCycleNumber() {
        return this.cycleNumber;
    }

    public int getMaxHp() {
        return this.maxHp;
    }

    public int getRemainingHp() {
        return this.remainingHp;
    }

    public WorldBossStatus getStatus() {
        return this.status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Instant getDefeatedAt() {
        return this.defeatedAt;
    }

    public Instant getDailyResetAt() {
        return this.dailyResetAt;
    }

    public long getVersion() {
        return this.version;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setBossId(final Long bossId) {
        this.bossId = bossId;
    }

    public void setBossDate(final LocalDate bossDate) {
        this.bossDate = bossDate;
    }

    public void setCycleNumber(final int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public void setMaxHp(final int maxHp) {
        this.maxHp = maxHp;
    }

    public void setRemainingHp(final int remainingHp) {
        this.remainingHp = remainingHp;
    }

    public void setStatus(final WorldBossStatus status) {
        this.status = status;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDefeatedAt(final Instant defeatedAt) {
        this.defeatedAt = defeatedAt;
    }

    public void setDailyResetAt(final Instant dailyResetAt) {
        this.dailyResetAt = dailyResetAt;
    }

    public void setVersion(final long version) {
        this.version = version;
    }

    public WorldBossInstance() {
        this.cycleNumber = WorldBossInstance.$default$cycleNumber();
        this.version = WorldBossInstance.$default$version();
    }

    public WorldBossInstance(final UUID id, final Long bossId, final LocalDate bossDate, final int cycleNumber, final int maxHp, final int remainingHp, final WorldBossStatus status, final Instant createdAt, final Instant updatedAt, final Instant defeatedAt, final Instant dailyResetAt, final long version) {
        this.id = id;
        this.bossId = bossId;
        this.bossDate = bossDate;
        this.cycleNumber = cycleNumber;
        this.maxHp = maxHp;
        this.remainingHp = remainingHp;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.defeatedAt = defeatedAt;
        this.dailyResetAt = dailyResetAt;
        this.version = version;
    }
}
