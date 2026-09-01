package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.AreaResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.AreaRules;
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

    public List<AreaResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Stage highestOwnedStage = digimonRepository.findByPlayerId(playerId).stream()
                .filter(digimon -> digimon.getStatus() != DigimonStatus.REBORN
                        && digimon.getStatus() != DigimonStatus.SACRIFICED
                        && digimon.getStatus() != DigimonStatus.COLLECTION_CONSUMED)
                .map(Digimon::getStage)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.comparingInt(Stage::ordinal))
                .orElse(Stage.BABY);
        return Arrays.stream(Area.values())
                .map(area -> new AreaResponse(
                        area,
                        AreaRules.requiredStage(area),
                        AreaRules.isUnlocked(highestOwnedStage, area)
                ))
                .toList();
    }

    public AreaUseCase(final DigimonRepository digimonRepository) {
        this.digimonRepository = digimonRepository;
    }
}
