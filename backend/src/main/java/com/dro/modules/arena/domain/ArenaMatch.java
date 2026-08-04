package com.dro.modules.arena.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "arena_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
