package com.dro.modules.boss.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "boss_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BossAttemptEntity {

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BossAttemptStatus status;

    @Column(name = "damage_dealt", nullable = false)
    private int damageDealt;

    @Column(name = "xp_gained", nullable = false)
    private int xpGained;

    @Column(name = "bits_gained", nullable = false)
    private int bitsGained;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
