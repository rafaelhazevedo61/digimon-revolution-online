package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionTeam;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionTeamRepository extends JpaRepository<MissionTeam, UUID> {
    List<MissionTeam> findByPlayerIdOrderByCreatedAtAsc(UUID playerId);

    Optional<MissionTeam> findByIdAndPlayerId(UUID id, UUID playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM MissionTeam t WHERE t.id = :id AND t.playerId = :playerId")
    Optional<MissionTeam> findByIdAndPlayerIdForUpdate(@Param("id") UUID id, @Param("playerId") UUID playerId);
}
