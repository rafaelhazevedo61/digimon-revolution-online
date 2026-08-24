package com.dro.modules.boss.world.domain;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Registro oficial de uma recompensa em Baú concedida no ciclo diário do Boss Mundial.
 *
 * <p>A linha funciona como a fonte de verdade da concessão. O campo
 * {@code eventKey} impede que uma repetição do mesmo ataque distribua novamente
 * um Baú de tentativa ou uma recompensa de encerramento.</p>
 */
@Entity
@Table(name = "world_boss_rewards", uniqueConstraints = {@UniqueConstraint(name = "uk_world_boss_rewards_event_key", columnNames = "event_key")})
public class WorldBossReward {
    @Id
    private UUID id;
    @Column(name = "world_boss_id", nullable = false)
    private UUID worldBossId;
    @Column(name = "source_attack_id", nullable = false)
    private UUID sourceAttackId;
    @Column(name = "recipient_player_id", nullable = false)
    private UUID recipientPlayerId;
    @Column(name = "recipient_digimon_id", nullable = false)
    private UUID recipientDigimonId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chest_definition_id", nullable = false)

    private ChestDefinitionEntity chestDefinition;
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private WorldBossRewardType rewardType;
    @Column(name = "event_key", nullable = false, length = 180)
    private String eventKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public static class WorldBossRewardBuilder {
        private UUID id;
        private UUID worldBossId;
        private UUID sourceAttackId;
        private UUID recipientPlayerId;
        private UUID recipientDigimonId;
        private ChestDefinitionEntity chestDefinition;
        private WorldBossRewardType rewardType;
        private String eventKey;
        private Instant createdAt;

        WorldBossRewardBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder worldBossId(final UUID worldBossId) {
            this.worldBossId = worldBossId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder sourceAttackId(final UUID sourceAttackId) {
            this.sourceAttackId = sourceAttackId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder recipientPlayerId(final UUID recipientPlayerId) {
            this.recipientPlayerId = recipientPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder recipientDigimonId(final UUID recipientDigimonId) {
            this.recipientDigimonId = recipientDigimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder chestDefinition(final ChestDefinitionEntity chestDefinition) {
            this.chestDefinition = chestDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder rewardType(final WorldBossRewardType rewardType) {
            this.rewardType = rewardType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder eventKey(final String eventKey) {
            this.eventKey = eventKey;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WorldBossReward.WorldBossRewardBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorldBossReward build() {
            return new WorldBossReward(this.id, this.worldBossId, this.sourceAttackId, this.recipientPlayerId, this.recipientDigimonId, this.chestDefinition, this.rewardType, this.eventKey, this.createdAt);
        }

        @Override
        public String toString() {
            return "WorldBossReward.WorldBossRewardBuilder(id=" + this.id + ", worldBossId=" + this.worldBossId + ", sourceAttackId=" + this.sourceAttackId + ", recipientPlayerId=" + this.recipientPlayerId + ", recipientDigimonId=" + this.recipientDigimonId + ", chestDefinition=" + this.chestDefinition + ", rewardType=" + this.rewardType + ", eventKey=" + this.eventKey + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static WorldBossReward.WorldBossRewardBuilder builder() {
        return new WorldBossReward.WorldBossRewardBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getWorldBossId() {
        return this.worldBossId;
    }

    public UUID getSourceAttackId() {
        return this.sourceAttackId;
    }

    public UUID getRecipientPlayerId() {
        return this.recipientPlayerId;
    }

    public UUID getRecipientDigimonId() {
        return this.recipientDigimonId;
    }

    public ChestDefinitionEntity getChestDefinition() {
        return this.chestDefinition;
    }

    public WorldBossRewardType getRewardType() {
        return this.rewardType;
    }

    public String getEventKey() {
        return this.eventKey;
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

    public void setSourceAttackId(final UUID sourceAttackId) {
        this.sourceAttackId = sourceAttackId;
    }

    public void setRecipientPlayerId(final UUID recipientPlayerId) {
        this.recipientPlayerId = recipientPlayerId;
    }

    public void setRecipientDigimonId(final UUID recipientDigimonId) {
        this.recipientDigimonId = recipientDigimonId;
    }

    public void setChestDefinition(final ChestDefinitionEntity chestDefinition) {
        this.chestDefinition = chestDefinition;
    }

    public void setRewardType(final WorldBossRewardType rewardType) {
        this.rewardType = rewardType;
    }

    public void setEventKey(final String eventKey) {
        this.eventKey = eventKey;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public WorldBossReward() {
    }

    public WorldBossReward(final UUID id, final UUID worldBossId, final UUID sourceAttackId, final UUID recipientPlayerId, final UUID recipientDigimonId, final ChestDefinitionEntity chestDefinition, final WorldBossRewardType rewardType, final String eventKey, final Instant createdAt) {
        this.id = id;
        this.worldBossId = worldBossId;
        this.sourceAttackId = sourceAttackId;
        this.recipientPlayerId = recipientPlayerId;
        this.recipientDigimonId = recipientDigimonId;
        this.chestDefinition = chestDefinition;
        this.rewardType = rewardType;
        this.eventKey = eventKey;
        this.createdAt = createdAt;
    }
}
