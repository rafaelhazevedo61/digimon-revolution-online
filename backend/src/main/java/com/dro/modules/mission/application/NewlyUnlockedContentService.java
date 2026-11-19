package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedAreaResponse;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedContentResponse;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedMissionResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.AreaRules;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class NewlyUnlockedContentService {
    private final MissionDefinitionRepository missionDefinitionRepository;

    public NewlyUnlockedContentService(MissionDefinitionRepository missionDefinitionRepository) {
        this.missionDefinitionRepository = missionDefinitionRepository;
    }

    public NewlyUnlockedContentResponse detect(Digimon digimon, int previousLevel, Stage previousStage) {
        if (digimon == null || digimon.getLevel() <= previousLevel && digimon.getStage() == previousStage) {
            return NewlyUnlockedContentResponse.empty();
        }

        int currentLevel = digimon.getLevel();
        Stage currentStage = digimon.getStage();

        var activeMissions = missionDefinitionRepository.findByActiveTrue();
        List<NewlyUnlockedMissionResponse> missions = activeMissions.stream()
                .filter(mission -> mission.getRequiredLevel() > previousLevel)
                .filter(mission -> mission.getRequiredLevel() <= currentLevel)
                .filter(mission -> currentStage.ordinal() >= mission.getRequiredStage().ordinal())
                .sorted(Comparator.comparingInt(MissionDefinitionEntity::getRequiredLevel)
                        .thenComparing(MissionDefinitionEntity::getId))
                .map(mission -> new NewlyUnlockedMissionResponse(
                        mission.getId(),
                        mission.getName(),
                        mission.getArea().name(),
                        mission.getRequiredLevel()
                ))
                .toList();

        List<NewlyUnlockedAreaResponse> areas = new ArrayList<>(Arrays.stream(Area.values())
                .filter(area -> area != Area.GEAR_SAVANNA)
                .filter(area -> previousStage.ordinal() < AreaRules.requiredStage(area).ordinal())
                .filter(area -> currentStage.ordinal() >= AreaRules.requiredStage(area).ordinal())
                .map(area -> new NewlyUnlockedAreaResponse(area.name(), AreaRules.requiredStage(area).name()))
                .toList());
        activeMissions.stream()
                .filter(mission -> AreaRules.GEAR_SAVANNA_ENTRY_MISSION.equals(mission.getId())
                        && mission.getArea() == Area.GEAR_SAVANNA)
                .filter(mission -> !AreaRules.meetsMissionRequirements(previousStage, previousLevel, mission)
                        && AreaRules.meetsMissionRequirements(currentStage, currentLevel, mission))
                .findFirst().ifPresent(mission -> areas.add(new NewlyUnlockedAreaResponse(
                        Area.GEAR_SAVANNA.name(), mission.getRequiredStage().name())));

        return new NewlyUnlockedContentResponse(missions, areas);
    }
}
