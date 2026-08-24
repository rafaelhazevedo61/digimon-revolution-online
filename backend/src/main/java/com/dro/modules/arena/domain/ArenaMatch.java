package com.dro.modules.arena.domain;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Arena.
 */
@Entity
@Table(name = "arena_matches")
public class ArenaMatch {
    @Id
    private UUID id;
    @Column(name = "attacker_player_id", nullable = false)
    private UUID attackerPlayerId;
    @Column(name = "attacker_digimon_id", nullable = false)
    private UUID attackerDigimonId;
    @Column(name = "defender_player_id", nullable = false)
    private UUID defenderPlayerId;
    @Column(name = "defender_digimon_id", nullable = false)
    private UUID defenderDigimonId;
    @Column(name = "attacker_won", nullable = false)
    private boolean attackerWon;
    @Column(name = "attacker_power", nullable = false)
    private int attackerPower;
    @Column(name = "defender_power", nullable = false)
    private int defenderPower;
    @Column(name = "win_chance", nullable = false)
    private int winChance;
    @Column(name = "attacker_rating_change", nullable = false)
    private int attackerRatingChange;
    @Column(name = "attacker_rating_after", nullable = false)
    private int attackerRatingAfter;
    @Column(name = "defender_rating_change", nullable = false)
    private int defenderRatingChange;
    @Column(name = "defender_rating_after", nullable = false)
    private int defenderRatingAfter;
    @Column(name = "bits_gained", nullable = false)
    private int bitsGained;
    /**
     * Baú concedido ao atacante quando a partida terminou em vitória.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_chest_definition_id")

    private ChestDefinitionEntity rewardChest;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public static class ArenaMatchBuilder {
        private UUID id;
        private UUID attackerPlayerId;
        private UUID attackerDigimonId;
        private UUID defenderPlayerId;
        private UUID defenderDigimonId;
        private boolean attackerWon;
        private int attackerPower;
        private int defenderPower;
        private int winChance;
        private int attackerRatingChange;
        private int attackerRatingAfter;
        private int defenderRatingChange;
        private int defenderRatingAfter;
        private int bitsGained;
        private ChestDefinitionEntity rewardChest;
        private Instant createdAt;

        ArenaMatchBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerPlayerId(final UUID attackerPlayerId) {
            this.attackerPlayerId = attackerPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerDigimonId(final UUID attackerDigimonId) {
            this.attackerDigimonId = attackerDigimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder defenderPlayerId(final UUID defenderPlayerId) {
            this.defenderPlayerId = defenderPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder defenderDigimonId(final UUID defenderDigimonId) {
            this.defenderDigimonId = defenderDigimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerWon(final boolean attackerWon) {
            this.attackerWon = attackerWon;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerPower(final int attackerPower) {
            this.attackerPower = attackerPower;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder defenderPower(final int defenderPower) {
            this.defenderPower = defenderPower;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder winChance(final int winChance) {
            this.winChance = winChance;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerRatingChange(final int attackerRatingChange) {
            this.attackerRatingChange = attackerRatingChange;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder attackerRatingAfter(final int attackerRatingAfter) {
            this.attackerRatingAfter = attackerRatingAfter;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder defenderRatingChange(final int defenderRatingChange) {
            this.defenderRatingChange = defenderRatingChange;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder defenderRatingAfter(final int defenderRatingAfter) {
            this.defenderRatingAfter = defenderRatingAfter;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder bitsGained(final int bitsGained) {
            this.bitsGained = bitsGained;
            return this;
        }

        /**
         * Baú concedido ao atacante quando a partida terminou em vitória.
         * @return {@code this}.
         */
        @JsonIgnore
        public ArenaMatch.ArenaMatchBuilder rewardChest(final ChestDefinitionEntity rewardChest) {
            this.rewardChest = rewardChest;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaMatch.ArenaMatchBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ArenaMatch build() {
            return new ArenaMatch(this.id, this.attackerPlayerId, this.attackerDigimonId, this.defenderPlayerId, this.defenderDigimonId, this.attackerWon, this.attackerPower, this.defenderPower, this.winChance, this.attackerRatingChange, this.attackerRatingAfter, this.defenderRatingChange, this.defenderRatingAfter, this.bitsGained, this.rewardChest, this.createdAt);
        }

        @Override
        public String toString() {
            return "ArenaMatch.ArenaMatchBuilder(id=" + this.id + ", attackerPlayerId=" + this.attackerPlayerId + ", attackerDigimonId=" + this.attackerDigimonId + ", defenderPlayerId=" + this.defenderPlayerId + ", defenderDigimonId=" + this.defenderDigimonId + ", attackerWon=" + this.attackerWon + ", attackerPower=" + this.attackerPower + ", defenderPower=" + this.defenderPower + ", winChance=" + this.winChance + ", attackerRatingChange=" + this.attackerRatingChange + ", attackerRatingAfter=" + this.attackerRatingAfter + ", defenderRatingChange=" + this.defenderRatingChange + ", defenderRatingAfter=" + this.defenderRatingAfter + ", bitsGained=" + this.bitsGained + ", rewardChest=" + this.rewardChest + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static ArenaMatch.ArenaMatchBuilder builder() {
        return new ArenaMatch.ArenaMatchBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getAttackerPlayerId() {
        return this.attackerPlayerId;
    }

    public UUID getAttackerDigimonId() {
        return this.attackerDigimonId;
    }

    public UUID getDefenderPlayerId() {
        return this.defenderPlayerId;
    }

    public UUID getDefenderDigimonId() {
        return this.defenderDigimonId;
    }

    public boolean isAttackerWon() {
        return this.attackerWon;
    }

    public int getAttackerPower() {
        return this.attackerPower;
    }

    public int getDefenderPower() {
        return this.defenderPower;
    }

    public int getWinChance() {
        return this.winChance;
    }

    public int getAttackerRatingChange() {
        return this.attackerRatingChange;
    }

    public int getAttackerRatingAfter() {
        return this.attackerRatingAfter;
    }

    public int getDefenderRatingChange() {
        return this.defenderRatingChange;
    }

    public int getDefenderRatingAfter() {
        return this.defenderRatingAfter;
    }

    public int getBitsGained() {
        return this.bitsGained;
    }

    /**
     * Baú concedido ao atacante quando a partida terminou em vitória.
     */
    public ChestDefinitionEntity getRewardChest() {
        return this.rewardChest;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setAttackerPlayerId(final UUID attackerPlayerId) {
        this.attackerPlayerId = attackerPlayerId;
    }

    public void setAttackerDigimonId(final UUID attackerDigimonId) {
        this.attackerDigimonId = attackerDigimonId;
    }

    public void setDefenderPlayerId(final UUID defenderPlayerId) {
        this.defenderPlayerId = defenderPlayerId;
    }

    public void setDefenderDigimonId(final UUID defenderDigimonId) {
        this.defenderDigimonId = defenderDigimonId;
    }

    public void setAttackerWon(final boolean attackerWon) {
        this.attackerWon = attackerWon;
    }

    public void setAttackerPower(final int attackerPower) {
        this.attackerPower = attackerPower;
    }

    public void setDefenderPower(final int defenderPower) {
        this.defenderPower = defenderPower;
    }

    public void setWinChance(final int winChance) {
        this.winChance = winChance;
    }

    public void setAttackerRatingChange(final int attackerRatingChange) {
        this.attackerRatingChange = attackerRatingChange;
    }

    public void setAttackerRatingAfter(final int attackerRatingAfter) {
        this.attackerRatingAfter = attackerRatingAfter;
    }

    public void setDefenderRatingChange(final int defenderRatingChange) {
        this.defenderRatingChange = defenderRatingChange;
    }

    public void setDefenderRatingAfter(final int defenderRatingAfter) {
        this.defenderRatingAfter = defenderRatingAfter;
    }

    public void setBitsGained(final int bitsGained) {
        this.bitsGained = bitsGained;
    }

    /**
     * Baú concedido ao atacante quando a partida terminou em vitória.
     */
    public void setRewardChest(final ChestDefinitionEntity rewardChest) {
        this.rewardChest = rewardChest;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ArenaMatch() {
    }

    /**
     * Creates a new {@code ArenaMatch} instance.
     *
     * @param id
     * @param attackerPlayerId
     * @param attackerDigimonId
     * @param defenderPlayerId
     * @param defenderDigimonId
     * @param attackerWon
     * @param attackerPower
     * @param defenderPower
     * @param winChance
     * @param attackerRatingChange
     * @param attackerRatingAfter
     * @param defenderRatingChange
     * @param defenderRatingAfter
     * @param bitsGained
     * @param rewardChest Baú concedido ao atacante quando a partida terminou em vitória.
     * @param createdAt
     */
    public ArenaMatch(final UUID id, final UUID attackerPlayerId, final UUID attackerDigimonId, final UUID defenderPlayerId, final UUID defenderDigimonId, final boolean attackerWon, final int attackerPower, final int defenderPower, final int winChance, final int attackerRatingChange, final int attackerRatingAfter, final int defenderRatingChange, final int defenderRatingAfter, final int bitsGained, final ChestDefinitionEntity rewardChest, final Instant createdAt) {
        this.id = id;
        this.attackerPlayerId = attackerPlayerId;
        this.attackerDigimonId = attackerDigimonId;
        this.defenderPlayerId = defenderPlayerId;
        this.defenderDigimonId = defenderDigimonId;
        this.attackerWon = attackerWon;
        this.attackerPower = attackerPower;
        this.defenderPower = defenderPower;
        this.winChance = winChance;
        this.attackerRatingChange = attackerRatingChange;
        this.attackerRatingAfter = attackerRatingAfter;
        this.defenderRatingChange = defenderRatingChange;
        this.defenderRatingAfter = defenderRatingAfter;
        this.bitsGained = bitsGained;
        this.rewardChest = rewardChest;
        this.createdAt = createdAt;
    }
}
