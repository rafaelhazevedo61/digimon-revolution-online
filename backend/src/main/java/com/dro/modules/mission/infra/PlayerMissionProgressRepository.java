package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.PlayerMissionProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerMissionProgressRepository
        extends JpaRepository<PlayerMissionProgress, UUID> {

    Optional<PlayerMissionProgress> findByPlayerIdAndMissionId(
            UUID playerId,
            String missionId
    );
}
