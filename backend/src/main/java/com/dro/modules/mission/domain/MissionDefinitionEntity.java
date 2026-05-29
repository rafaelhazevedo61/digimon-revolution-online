package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDefinitionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_stage", nullable = false)
    private Stage requiredStage;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel;

    @Column(name = "base_xp", nullable = false)
    private int baseXp;

    @Column(name = "energy_cost", nullable = false)
    private int energyCost;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "missionId", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionRewardEntity> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "missionId", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootChanceEntity> lootChances = new ArrayList<>();

    @OneToMany(mappedBy = "missionId", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootItemEntity> lootItems = new ArrayList<>();
}
