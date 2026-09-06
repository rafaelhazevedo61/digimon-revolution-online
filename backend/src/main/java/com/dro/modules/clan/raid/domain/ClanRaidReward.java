package com.dro.modules.clan.raid.domain;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro oficial de uma recompensa em Baú concedida no ciclo de uma incursão.
 * O {@code eventKey} impede a duplicação em reprocessamentos transacionais.
 */
@Entity
@Table(name = "clan_raid_rewards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_clan_raid_rewards_event_key", columnNames = "event_key")
})
public class ClanRaidReward {
    @Id
    private UUID id;

    @Column(name = "clan_raid_id", nullable = false)
    private UUID clanRaidId;

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
    private ClanRaidRewardType rewardType;

    @Column(name = "event_key", nullable = false, length = 180)
    private String eventKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static class ClanRaidRewardBuilder {
        private UUID id;
        private UUID clanRaidId;
        private UUID sourceAttackId;
        private UUID recipientPlayerId;
        private UUID recipientDigimonId;
        private ChestDefinitionEntity chestDefinition;
        private ClanRaidRewardType rewardType;
        private String eventKey;
        private Instant createdAt;

        ClanRaidRewardBuilder() {
        }

        public ClanRaidRewardBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ClanRaidRewardBuilder clanRaidId(UUID clanRaidId) {
            this.clanRaidId = clanRaidId;
            return this;
        }

        public ClanRaidRewardBuilder sourceAttackId(UUID sourceAttackId) {
            this.sourceAttackId = sourceAttackId;
            return this;
        }

        public ClanRaidRewardBuilder recipientPlayerId(UUID recipientPlayerId) {
            this.recipientPlayerId = recipientPlayerId;
            return this;
        }

        public ClanRaidRewardBuilder recipientDigimonId(UUID recipientDigimonId) {
            this.recipientDigimonId = recipientDigimonId;
            return this;
        }

        public ClanRaidRewardBuilder chestDefinition(ChestDefinitionEntity chestDefinition) {
            this.chestDefinition = chestDefinition;
            return this;
        }

        public ClanRaidRewardBuilder rewardType(ClanRaidRewardType rewardType) {
            this.rewardType = rewardType;
            return this;
        }

        public ClanRaidRewardBuilder eventKey(String eventKey) {
            this.eventKey = eventKey;
            return this;
        }

        public ClanRaidRewardBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ClanRaidReward build() {
            return new ClanRaidReward(id, clanRaidId, sourceAttackId, recipientPlayerId,
                    recipientDigimonId, chestDefinition, rewardType, eventKey, createdAt);
        }
    }

    public static ClanRaidRewardBuilder builder() {
        return new ClanRaidRewardBuilder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClanRaidId() {
        return clanRaidId;
    }

    public UUID getSourceAttackId() {
        return sourceAttackId;
    }

    public UUID getRecipientPlayerId() {
        return recipientPlayerId;
    }

    public UUID getRecipientDigimonId() {
        return recipientDigimonId;
    }

    public ChestDefinitionEntity getChestDefinition() {
        return chestDefinition;
    }

    public ClanRaidRewardType getRewardType() {
        return rewardType;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ClanRaidReward() {
    }

    public ClanRaidReward(UUID id, UUID clanRaidId, UUID sourceAttackId, UUID recipientPlayerId,
                          UUID recipientDigimonId, ChestDefinitionEntity chestDefinition,
                          ClanRaidRewardType rewardType, String eventKey, Instant createdAt) {
        this.id = id;
        this.clanRaidId = clanRaidId;
        this.sourceAttackId = sourceAttackId;
        this.recipientPlayerId = recipientPlayerId;
        this.recipientDigimonId = recipientDigimonId;
        this.chestDefinition = chestDefinition;
        this.rewardType = rewardType;
        this.eventKey = eventKey;
        this.createdAt = createdAt;
    }
}
