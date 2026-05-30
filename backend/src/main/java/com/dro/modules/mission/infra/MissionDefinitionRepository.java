package com.dro.modules.mission.infra;

import com.dro.modules.mission.domain.MissionDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionDefinitionRepository extends JpaRepository<MissionDefinitionEntity, String> {

    List<MissionDefinitionEntity> findByActiveTrue();
}
