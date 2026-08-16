package com.dro.modules.clan.raid.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clan_raid_attacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanRaidAttack {

    @Id
    private UUID id;

    @Column(name = "clan_raid_id", nullable = false)
    private UUID clanRaidId;

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
