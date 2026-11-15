package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.AreaResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.AreaRules;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class AreaUseCase {
    private final DigimonRepository digimonRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;

    public List<AreaResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        List<Digimon> owned = digimonRepository.findByPlayerId(playerId).stream()
                .filter(digimon -> digimon.getStatus() != DigimonStatus.REBORN
                        && digimon.getStatus() != DigimonStatus.SACRIFICED
                        && digimon.getStatus() != DigimonStatus.COLLECTION_CONSUMED)
                .toList();
        Stage highestOwnedStage = owned.stream()
                .map(Digimon::getStage)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.comparingInt(Stage::ordinal))
                .orElse(Stage.BABY);
        AreaResponse gearSavanna = missionDefinitionRepository.findByIdAndActiveTrue(AreaRules.GEAR_SAVANNA_ENTRY_MISSION)
                .filter(mission -> mission.getArea() == Area.GEAR_SAVANNA)
                .map(mission -> new AreaResponse(Area.GEAR_SAVANNA, mission.getRequiredStage(),
                        mission.getRequiredLevel(), owned.stream().anyMatch(digimon ->
                                AreaRules.meetsMissionRequirements(digimon.getStage(), digimon.getLevel(), mission))))
                .orElse(new AreaResponse(Area.GEAR_SAVANNA, null, null, false));
        return Arrays.stream(Area.values())
                .map(area -> area == Area.GEAR_SAVANNA ? gearSavanna : new AreaResponse(
                        area,
                        AreaRules.requiredStage(area),
                        null,
                        AreaRules.isUnlocked(highestOwnedStage, area)
                ))
                .toList();
    }

    public AreaUseCase(final DigimonRepository digimonRepository,
                       final MissionDefinitionRepository missionDefinitionRepository) {
        this.digimonRepository = digimonRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
    }
}
