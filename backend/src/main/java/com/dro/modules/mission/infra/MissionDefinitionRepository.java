package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Componente da camada de repositório de persistência do módulo de Missões.
 */
public interface MissionDefinitionRepository extends JpaRepository<MissionDefinitionEntity, String> {

    List<MissionDefinitionEntity> findByActiveTrue();

    Optional<MissionDefinitionEntity> findByIdAndActiveTrue(String id);

    List<MissionDefinitionEntity> findAllByOrderByNameAsc();

    List<MissionDefinitionEntity> findByActiveTrueOrderByNameAsc();
}
