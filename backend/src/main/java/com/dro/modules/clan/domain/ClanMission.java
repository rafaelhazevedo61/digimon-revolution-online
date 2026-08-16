package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "clan_missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanMission {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(length = 280)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "objective_type", nullable = false, length = 30)
    private ClanMissionObjectiveType objectiveType;

    @Column(name = "target_value", nullable = false)
    private int targetValue;

    @Column(name = "min_honor_marks_reward", nullable = false)
    private int minHonorMarksReward;

    @Column(name = "max_honor_marks_reward", nullable = false)
    private int maxHonorMarksReward;

    @Column(name = "clan_xp_reward", nullable = false)
    private int clanXpReward;

    @Column(name = "min_clan_level", nullable = false)
    @Builder.Default
    private int minClanLevel = 1;
}
