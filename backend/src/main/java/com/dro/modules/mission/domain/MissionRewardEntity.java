package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRewardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_id", nullable = false)
    private String missionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "base_quantity", nullable = false)
    private int baseQuantity;
}
