package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerClanMissionRepository extends JpaRepository<PlayerClanMission, UUID> {

    List<PlayerClanMission> findByPlayerId(UUID playerId);

    Optional<PlayerClanMission> findByPlayerIdAndStatus(UUID playerId, PlayerClanMissionStatus status);

    Optional<PlayerClanMission> findByPlayerIdAndStatusIn(UUID playerId, Collection<PlayerClanMissionStatus> statuses);

    boolean existsByPlayerIdAndClanMissionId(UUID playerId, UUID clanMissionId);
}
