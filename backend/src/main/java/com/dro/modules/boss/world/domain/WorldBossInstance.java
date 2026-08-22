package com.dro.modules.boss.world.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "world_boss_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorldBossInstance {

    @Id
    private UUID id;

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(name = "boss_date", nullable = false)
    private LocalDate bossDate;

    @Column(name = "cycle_number", nullable = false)
    @Builder.Default
    private int cycleNumber = 1;

    @Column(nullable = false)
    private int maxHp;

    @Column(name = "remaining_hp", nullable = false)
    private int remainingHp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorldBossStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "defeated_at")
    private Instant defeatedAt;

    @Column(name = "daily_reset_at")
    private Instant dailyResetAt;

    @Version
    @Builder.Default
    private long version = 0;
}
