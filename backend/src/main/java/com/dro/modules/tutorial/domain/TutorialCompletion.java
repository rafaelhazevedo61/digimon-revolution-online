package com.dro.modules.tutorial.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tutorial_completions")
public class TutorialCompletion {
    @Id
    private UUID playerId;
    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;


    public static class TutorialCompletionBuilder {
        private UUID playerId;
        private LocalDateTime finishedAt;

        TutorialCompletionBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public TutorialCompletion.TutorialCompletionBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TutorialCompletion.TutorialCompletionBuilder finishedAt(final LocalDateTime finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public TutorialCompletion build() {
            return new TutorialCompletion(this.playerId, this.finishedAt);
        }

        @Override
        public String toString() {
            return "TutorialCompletion.TutorialCompletionBuilder(playerId=" + this.playerId + ", finishedAt=" + this.finishedAt + ")";
        }
    }

    public static TutorialCompletion.TutorialCompletionBuilder builder() {
        return new TutorialCompletion.TutorialCompletionBuilder();
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public LocalDateTime getFinishedAt() {
        return this.finishedAt;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setFinishedAt(final LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public TutorialCompletion() {
    }

    public TutorialCompletion(final UUID playerId, final LocalDateTime finishedAt) {
        this.playerId = playerId;
        this.finishedAt = finishedAt;
    }
}
