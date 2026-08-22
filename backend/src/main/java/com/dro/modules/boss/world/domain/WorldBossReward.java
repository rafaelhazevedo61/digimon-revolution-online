package com.dro.modules.boss.world.domain;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "world_boss_rewards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_world_boss_rewards_event_key", columnNames = "event_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @ToString.Exclude
    private ChestDefinitionEntity chestDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private WorldBossRewardType rewardType;

    @Column(name = "event_key", nullable = false, length = 180)
    private String eventKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
