package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<MissionInstance> findByPlayerIdAndStatusIn(
            UUID playerId,
            List<MissionStatus> statuses
    );
}
