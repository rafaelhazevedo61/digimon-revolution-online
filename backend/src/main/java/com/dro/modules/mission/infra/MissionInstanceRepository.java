package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Missões.
 */
public interface MissionInstanceRepository
        extends JpaRepository<MissionInstance, UUID> {

    boolean existsByDigimonIdAndStatus(UUID digimonId, MissionStatus status);

    long countByDigimonIdAndStatus(UUID digimonId, MissionStatus status);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MissionInstance m " +
            "WHERE m.playerId = :playerId AND m.status IN :statuses " +
            "AND (m.digimonId IN :digimonIds OR m.digimon2Id IN :digimonIds OR m.digimon3Id IN :digimonIds)")
    boolean existsByPlayerIdAndAnyDigimonIdAndStatusIn(
            @Param("playerId") UUID playerId,
            @Param("digimonIds") List<UUID> digimonIds,
            @Param("statuses") List<MissionStatus> statuses
    );

    long countByPlayerIdAndStatusIn(UUID playerId, List<MissionStatus> statuses);

    boolean existsByTeamIdAndStatusIn(UUID teamId, List<MissionStatus> statuses);

    Optional<MissionInstance> findByIdAndPlayerId(UUID id, UUID playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MissionInstance m WHERE m.id = :id")
    Optional<MissionInstance> findByIdForUpdate(@Param("id") UUID id);

    List<MissionInstance> findByPlayerIdAndStatusIn(
            UUID playerId,
            List<MissionStatus> statuses
    );

    @Query("SELECT m.id FROM MissionInstance m WHERE m.autoClaimEnabled = true "
            + "AND m.status IN :statuses AND m.endsAt <= :now ORDER BY m.endsAt ASC")
    List<UUID> findIdsReadyForAutomaticClaim(
            @Param("statuses") List<MissionStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );
}
