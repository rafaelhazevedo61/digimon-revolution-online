package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
@RequiredArgsConstructor
public class ToggleMissionUseCase {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional
    public AdminMissionResponse execute(String id) {

        MissionDefinitionEntity entity = missionDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mission not found: " + id));

        entity.setActive(!entity.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        missionDefinitionRepository.saveAndFlush(entity);

        return AdminMissionResponse.from(entity);
    }
}
