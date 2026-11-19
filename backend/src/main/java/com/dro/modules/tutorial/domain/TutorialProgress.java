package com.dro.modules.tutorial.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Tutorial.
 */
@Entity
@Table(name = "tutorial_progress")
public class TutorialProgress {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialStep step;
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    @Column(name = "reward_claimed_at")
    private LocalDateTime rewardClaimedAt;


    public static class TutorialProgressBuilder {
        private UUID id;
        private UUID playerId;
        private TutorialStep step;
        private LocalDateTime completedAt;
        private LocalDateTime rewardClaimedAt;

        TutorialProgressBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public TutorialProgress.TutorialProgressBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TutorialProgress.TutorialProgressBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TutorialProgress.TutorialProgressBuilder step(final TutorialStep step) {
            this.step = step;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TutorialProgress.TutorialProgressBuilder completedAt(final LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TutorialProgress.TutorialProgressBuilder rewardClaimedAt(final LocalDateTime rewardClaimedAt) {
            this.rewardClaimedAt = rewardClaimedAt;
            return this;
        }

        public TutorialProgress build() {
            return new TutorialProgress(this.id, this.playerId, this.step, this.completedAt, this.rewardClaimedAt);
        }

        @Override
        public String toString() {
            return "TutorialProgress.TutorialProgressBuilder(id=" + this.id + ", playerId=" + this.playerId + ", step=" + this.step + ", completedAt=" + this.completedAt + ", rewardClaimedAt=" + this.rewardClaimedAt + ")";
        }
    }

    public static TutorialProgress.TutorialProgressBuilder builder() {
        return new TutorialProgress.TutorialProgressBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public TutorialStep getStep() {
        return this.step;
    }

    public LocalDateTime getCompletedAt() {
        return this.completedAt;
    }

    public LocalDateTime getRewardClaimedAt() {
        return this.rewardClaimedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setStep(final TutorialStep step) {
        this.step = step;
    }

    public void setCompletedAt(final LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setRewardClaimedAt(final LocalDateTime rewardClaimedAt) {
        this.rewardClaimedAt = rewardClaimedAt;
    }

    public TutorialProgress() {
    }

    public TutorialProgress(final UUID id, final UUID playerId, final TutorialStep step, final LocalDateTime completedAt, final LocalDateTime rewardClaimedAt) {
        this.id = id;
        this.playerId = playerId;
        this.step = step;
        this.completedAt = completedAt;
        this.rewardClaimedAt = rewardClaimedAt;
    }
}
