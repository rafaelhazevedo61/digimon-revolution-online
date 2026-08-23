package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface PlayerClanMissionRepository extends JpaRepository<PlayerClanMission, UUID> {

    List<PlayerClanMission> findByPlayerId(UUID playerId);

    Optional<PlayerClanMission> findByPlayerIdAndStatus(
            UUID playerId,
            PlayerClanMissionStatus status
    );

    Optional<PlayerClanMission> findByPlayerIdAndStatusIn(
            UUID playerId,
            Collection<PlayerClanMissionStatus> statuses
    );

    boolean existsByPlayerIdAndClanMissionId(
            UUID playerId,
            UUID clanMissionId
    );

    boolean existsByPlayerIdAndClanMissionIdAndAcceptedAtGreaterThanEqual(
            UUID playerId,
            UUID clanMissionId,
            LocalDateTime startOfDay
    );

    List<PlayerClanMission> findByPlayerIdAndAcceptedAtGreaterThanEqual(
            UUID playerId,
            LocalDateTime startOfDay
    );

    List<PlayerClanMission> findByClanIdAndStatus(
            UUID clanId,
            PlayerClanMissionStatus status
    );

    List<PlayerClanMission> findByStatus(PlayerClanMissionStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlayerClanMission mission
               set mission.status = :expiredStatus
             where mission.status = :inProgressStatus
               and mission.acceptedAt < :cutoff
            """)
    int expireInProgressAcceptedBefore(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("inProgressStatus") PlayerClanMissionStatus inProgressStatus,
            @Param("expiredStatus") PlayerClanMissionStatus expiredStatus
    );
}