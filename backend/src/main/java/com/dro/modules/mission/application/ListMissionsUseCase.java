package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ListMissionsUseCase {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional(readOnly = true)
    public List<AdminMissionResponse> execute(Boolean activeOnly, String area, String stage, String lootItemType) {
        var entities = Boolean.TRUE.equals(activeOnly)
                ? missionDefinitionRepository.findByActiveTrue()
                : missionDefinitionRepository.findAll();

        Stream<MissionDefinitionEntity> stream = entities.stream();

        if (area != null && !area.isBlank()) {
            Area areaEnum = Area.valueOf(area);
            stream = stream.filter(e -> e.getArea() == areaEnum);
        }

        if (stage != null && !stage.isBlank()) {
            stream = stream.filter(e -> e.getRequiredStage().name().equals(stage));
        }

        if (lootItemType != null && !lootItemType.isBlank()) {
            stream = stream.filter(e ->
                    e.getRewards().stream().anyMatch(r -> r.getItemType().name().equals(lootItemType))
                    || e.getLootItems().stream().anyMatch(li -> li.getItemType().name().equals(lootItemType)));
        }

        return stream
                .sorted(Comparator.comparingInt((MissionDefinitionEntity e) -> e.getArea().ordinal())
                        .thenComparingInt(MissionDefinitionEntity::getRequiredLevel))
                .map(AdminMissionResponse::from)
                .toList();
    }
}
