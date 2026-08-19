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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
