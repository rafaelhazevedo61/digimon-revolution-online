package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_clan_missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerClanMission {

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "clan_mission_id", nullable = false)
    private UUID clanMissionId;

    @Column(name = "clan_id", nullable = false)
    private UUID clanId;

    @Column(nullable = false)
    @Builder.Default
    private int progress = 0;

    @Column(name = "honor_marks_reward", nullable = false)
    @Builder.Default
    private int honorMarksReward = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PlayerClanMissionStatus status = PlayerClanMissionStatus.IN_PROGRESS;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
