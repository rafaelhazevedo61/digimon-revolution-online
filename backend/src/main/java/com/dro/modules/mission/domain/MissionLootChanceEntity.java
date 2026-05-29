package com.dro.modules.mission.domain;

import com.dro.modules.loot.domain.LootRarity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_loot_chances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionLootChanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_id", nullable = false)
    private String missionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LootRarity rarity;

    @Column(nullable = false)
    private int chance;
}
