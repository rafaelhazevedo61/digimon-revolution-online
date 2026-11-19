package com.dro.modules.mission.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "player_mission_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "mission_id"}))
public class PlayerMissionProgress {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Column(name = "mission_id", nullable = false)
    private String missionId;
    @Column(name = "completion_count", nullable = false)
    private int completionCount;


    public static class PlayerMissionProgressBuilder {
        private UUID id;
        private UUID playerId;
        private String missionId;
        private int completionCount;

        PlayerMissionProgressBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public PlayerMissionProgress.PlayerMissionProgressBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerMissionProgress.PlayerMissionProgressBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerMissionProgress.PlayerMissionProgressBuilder missionId(final String missionId) {
            this.missionId = missionId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerMissionProgress.PlayerMissionProgressBuilder completionCount(final int completionCount) {
            this.completionCount = completionCount;
            return this;
        }

        public PlayerMissionProgress build() {
            return new PlayerMissionProgress(this.id, this.playerId, this.missionId, this.completionCount);
        }

        @Override
        public String toString() {
            return "PlayerMissionProgress.PlayerMissionProgressBuilder(id=" + this.id + ", playerId=" + this.playerId + ", missionId=" + this.missionId + ", completionCount=" + this.completionCount + ")";
        }
    }

    public static PlayerMissionProgress.PlayerMissionProgressBuilder builder() {
        return new PlayerMissionProgress.PlayerMissionProgressBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getMissionId() {
        return this.missionId;
    }

    public int getCompletionCount() {
        return this.completionCount;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setMissionId(final String missionId) {
        this.missionId = missionId;
    }

    public void setCompletionCount(final int completionCount) {
        this.completionCount = completionCount;
    }

    public PlayerMissionProgress() {
    }

    public PlayerMissionProgress(final UUID id, final UUID playerId, final String missionId, final int completionCount) {
        this.id = id;
        this.playerId = playerId;
        this.missionId = missionId;
        this.completionCount = completionCount;
    }
}
