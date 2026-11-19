package com.dro.modules.clan.raid.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_raid_instances")
public class ClanRaid {
    @Id
    private UUID id;
    @Column(name = "clan_id", nullable = false)
    private UUID clanId;
    @Column(name = "boss_id", nullable = false)
    private Long bossId;
    @Column(nullable = false)
    private int maxHp;
    @Column(name = "remaining_hp", nullable = false)
    private int remainingHp;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClanRaidStatus status;
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

    private static long $default$version() {
        return 0;
    }


    public static class ClanRaidBuilder {
        private UUID id;
        private UUID clanId;
        private Long bossId;
        private int maxHp;
        private int remainingHp;
        private ClanRaidStatus status;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant defeatedAt;
        private Instant dailyResetAt;
        private boolean version$set;
        private long version$value;

        ClanRaidBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder clanId(final UUID clanId) {
            this.clanId = clanId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder bossId(final Long bossId) {
            this.bossId = bossId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder maxHp(final int maxHp) {
            this.maxHp = maxHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder remainingHp(final int remainingHp) {
            this.remainingHp = remainingHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder status(final ClanRaidStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder defeatedAt(final Instant defeatedAt) {
            this.defeatedAt = defeatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder dailyResetAt(final Instant dailyResetAt) {
            this.dailyResetAt = dailyResetAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanRaid.ClanRaidBuilder version(final long version) {
            this.version$value = version;
            version$set = true;
            return this;
        }

        public ClanRaid build() {
            long version$value = this.version$value;
            if (!this.version$set) version$value = ClanRaid.$default$version();
            return new ClanRaid(this.id, this.clanId, this.bossId, this.maxHp, this.remainingHp, this.status, this.createdAt, this.updatedAt, this.defeatedAt, this.dailyResetAt, version$value);
        }

        @Override
        public String toString() {
            return "ClanRaid.ClanRaidBuilder(id=" + this.id + ", clanId=" + this.clanId + ", bossId=" + this.bossId + ", maxHp=" + this.maxHp + ", remainingHp=" + this.remainingHp + ", status=" + this.status + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", defeatedAt=" + this.defeatedAt + ", dailyResetAt=" + this.dailyResetAt + ", version$value=" + this.version$value + ")";
        }
    }

    public static ClanRaid.ClanRaidBuilder builder() {
        return new ClanRaid.ClanRaidBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getClanId() {
        return this.clanId;
    }

    public Long getBossId() {
        return this.bossId;
    }

    public int getMaxHp() {
        return this.maxHp;
    }

    public int getRemainingHp() {
        return this.remainingHp;
    }

    public ClanRaidStatus getStatus() {
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

    public void setClanId(final UUID clanId) {
        this.clanId = clanId;
    }

    public void setBossId(final Long bossId) {
        this.bossId = bossId;
    }

    public void setMaxHp(final int maxHp) {
        this.maxHp = maxHp;
    }

    public void setRemainingHp(final int remainingHp) {
        this.remainingHp = remainingHp;
    }

    public void setStatus(final ClanRaidStatus status) {
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

    public ClanRaid() {
        this.version = ClanRaid.$default$version();
    }

    public ClanRaid(final UUID id, final UUID clanId, final Long bossId, final int maxHp, final int remainingHp, final ClanRaidStatus status, final Instant createdAt, final Instant updatedAt, final Instant defeatedAt, final Instant dailyResetAt, final long version) {
        this.id = id;
        this.clanId = clanId;
        this.bossId = bossId;
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
