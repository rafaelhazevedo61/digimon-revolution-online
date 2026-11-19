package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "player_clan_missions")
public class PlayerClanMission {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "clan_mission_id", nullable = false)
    private UUID clanMissionId;
    @Column(name = "clan_id", nullable = false)
    private UUID clanId;
    @Column(nullable = false)
    private int progress;
    @Column(name = "honor_marks_reward", nullable = false)
    private int honorMarksReward;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlayerClanMissionStatus status;
    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private static int $default$progress() {
        return 0;
    }

    private static int $default$honorMarksReward() {
        return 0;
    }

    private static PlayerClanMissionStatus $default$status() {
        return PlayerClanMissionStatus.IN_PROGRESS;
    }


    public static class PlayerClanMissionBuilder {
        private UUID id;
        private UUID playerId;
        private UUID clanMissionId;
        private UUID clanId;
        private boolean progress$set;
        private int progress$value;
        private boolean honorMarksReward$set;
        private int honorMarksReward$value;
        private boolean status$set;
        private PlayerClanMissionStatus status$value;
        private LocalDateTime acceptedAt;
        private LocalDateTime completedAt;

        PlayerClanMissionBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder clanMissionId(final UUID clanMissionId) {
            this.clanMissionId = clanMissionId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder clanId(final UUID clanId) {
            this.clanId = clanId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder progress(final int progress) {
            this.progress$value = progress;
            progress$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder honorMarksReward(final int honorMarksReward) {
            this.honorMarksReward$value = honorMarksReward;
            honorMarksReward$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder status(final PlayerClanMissionStatus status) {
            this.status$value = status;
            status$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder acceptedAt(final LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerClanMission.PlayerClanMissionBuilder completedAt(final LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public PlayerClanMission build() {
            int progress$value = this.progress$value;
            if (!this.progress$set) progress$value = PlayerClanMission.$default$progress();
            int honorMarksReward$value = this.honorMarksReward$value;
            if (!this.honorMarksReward$set) honorMarksReward$value = PlayerClanMission.$default$honorMarksReward();
            PlayerClanMissionStatus status$value = this.status$value;
            if (!this.status$set) status$value = PlayerClanMission.$default$status();
            return new PlayerClanMission(this.id, this.playerId, this.clanMissionId, this.clanId, progress$value, honorMarksReward$value, status$value, this.acceptedAt, this.completedAt);
        }

        @Override
        public String toString() {
            return "PlayerClanMission.PlayerClanMissionBuilder(id=" + this.id + ", playerId=" + this.playerId + ", clanMissionId=" + this.clanMissionId + ", clanId=" + this.clanId + ", progress$value=" + this.progress$value + ", honorMarksReward$value=" + this.honorMarksReward$value + ", status$value=" + this.status$value + ", acceptedAt=" + this.acceptedAt + ", completedAt=" + this.completedAt + ")";
        }
    }

    public static PlayerClanMission.PlayerClanMissionBuilder builder() {
        return new PlayerClanMission.PlayerClanMissionBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public UUID getClanMissionId() {
        return this.clanMissionId;
    }

    public UUID getClanId() {
        return this.clanId;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getHonorMarksReward() {
        return this.honorMarksReward;
    }

    public PlayerClanMissionStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getAcceptedAt() {
        return this.acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return this.completedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setClanMissionId(final UUID clanMissionId) {
        this.clanMissionId = clanMissionId;
    }

    public void setClanId(final UUID clanId) {
        this.clanId = clanId;
    }

    public void setProgress(final int progress) {
        this.progress = progress;
    }

    public void setHonorMarksReward(final int honorMarksReward) {
        this.honorMarksReward = honorMarksReward;
    }

    public void setStatus(final PlayerClanMissionStatus status) {
        this.status = status;
    }

    public void setAcceptedAt(final LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public void setCompletedAt(final LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public PlayerClanMission() {
        this.progress = PlayerClanMission.$default$progress();
        this.honorMarksReward = PlayerClanMission.$default$honorMarksReward();
        this.status = PlayerClanMission.$default$status();
    }

    public PlayerClanMission(final UUID id, final UUID playerId, final UUID clanMissionId, final UUID clanId, final int progress, final int honorMarksReward, final PlayerClanMissionStatus status, final LocalDateTime acceptedAt, final LocalDateTime completedAt) {
        this.id = id;
        this.playerId = playerId;
        this.clanMissionId = clanMissionId;
        this.clanId = clanId;
        this.progress = progress;
        this.honorMarksReward = honorMarksReward;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.completedAt = completedAt;
    }
}
