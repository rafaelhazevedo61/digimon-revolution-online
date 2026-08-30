package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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

    Optional<MissionInstance> findByIdAndPlayerId(UUID id, UUID playerId);

    List<MissionInstance> findByPlayerIdAndStatusIn(
            UUID playerId,
            List<MissionStatus> statuses
    );
}
