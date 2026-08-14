package com.dro.modules.player.domain;

import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private DigitamaType selectedDigitama;

    @Column(name = "active_digimon_id")
    private UUID activeDigimonId;

    @Column(name = "last_mission_at")
    private LocalDateTime lastMissionAt;

    @Column(name = "has_selected_starter", nullable = false)
    private boolean starterSelected;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    @Builder.Default
    private UserType userType = UserType.PLAYER;

    @Column(name = "max_digimon_slots", nullable = false)
    private int maxDigimonSlots = 3;

    @Column(name = "max_storage_slots", nullable = false)
    private int maxStorageSlots = 50;

    @Column(name = "arena_coins", nullable = false)
    @Builder.Default
    private int arenaCoins = 0;

    @Column(name = "clan_id")
    private UUID clanId;

    @Column(name = "clan_role")
    @Enumerated(EnumType.STRING)
    private ClanRole clanRole;

    @Column(name = "clan_joined_at")
    private LocalDateTime clanJoinedAt;

    public boolean hasSelectedStarter() {
        return starterSelected;
    }

    public void markStarterAsSelected() {
        this.starterSelected = true;
    }

}
