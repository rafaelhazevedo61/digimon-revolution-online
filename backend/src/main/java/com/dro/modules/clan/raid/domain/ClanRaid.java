package com.dro.modules.clan.raid.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clan_raid_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanRaid {

    @Id
    private UUID id;

    @Column(name = "clan_id", nullable = false)
    private UUID clanId;

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(nullable = false)
    private int maxHp;

    @Column(name = "remaining_hp", nullable = false)
    private int remainingHp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClanRaidStatus status;

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
