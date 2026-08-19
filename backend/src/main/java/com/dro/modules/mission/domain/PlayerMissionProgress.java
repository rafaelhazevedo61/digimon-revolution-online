package com.dro.modules.mission.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "player_mission_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "mission_id"})
)
/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerMissionProgress {

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "mission_id", nullable = false)
    private String missionId;

    @Column(name = "completion_count", nullable = false)
    private int completionCount;
}
