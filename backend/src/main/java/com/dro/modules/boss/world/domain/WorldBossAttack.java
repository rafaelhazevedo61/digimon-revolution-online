package com.dro.modules.boss.world.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "world_boss_attacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorldBossAttack {

    @Id
    private UUID id;

    @Column(name = "world_boss_id", nullable = false)
    private UUID worldBossId;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;

    @Column(nullable = false)
    private int damage;

    @Column(name = "energy_cost", nullable = false)
    private int energyCost;

    @Column(name = "bits_gained", nullable = false)
    private int bitsGained;

    @Column(name = "xp_gained", nullable = false)
    private int xpGained;

    /** Chave fornecida pelo cliente para tornar o ataque idempotente. */
    @Column(name = "request_id", length = 120)
    private String requestId;

    @Column(name = "remaining_hp_after", nullable = false)
    private int remainingHpAfter;

    @Column(name = "win_chance", nullable = false)
    private int winChance;

    @Column(nullable = false)
    private boolean defeated;

    @Column(name = "defeated_reward_xp", nullable = false)
    private int defeatedRewardXp;

    @Column(name = "defeated_reward_bits", nullable = false)
    private int defeatedRewardBits;

    @Column(name = "daily_attacks_remaining", nullable = false)
    private int dailyAttacksRemaining;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
