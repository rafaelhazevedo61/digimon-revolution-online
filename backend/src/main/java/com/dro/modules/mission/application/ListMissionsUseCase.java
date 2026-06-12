package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMissionsUseCase {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional(readOnly = true)
    public List<AdminMissionResponse> execute(Boolean activeOnly) {
        var entities = Boolean.TRUE.equals(activeOnly)
                ? missionDefinitionRepository.findByActiveTrueOrderByNameAsc()
                : missionDefinitionRepository.findAllByOrderByNameAsc();

        return entities.stream()
                .map(AdminMissionResponse::from)
                .toList();
    }
}
