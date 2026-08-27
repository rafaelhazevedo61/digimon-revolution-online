package com.dro.modules.mission.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class GetAvailableMissionsUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;

    public List<MissionResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        var digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        int level = digimon.getLevel();
        var stage = digimon.getStage();
        return missionDefinitionRepository.findByActiveTrue().stream()
                .filter(m -> level >= m.getRequiredLevel())
                .filter(m -> stage.ordinal() >= m.getRequiredStage().ordinal())
                .sorted(Comparator
                        .comparingInt((MissionDefinitionEntity mission) -> mission.getArea().ordinal())
                        .reversed()
                        .thenComparing(Comparator.comparingInt(GetAvailableMissionsUseCase::missionNumber).reversed())
                        .thenComparing(Comparator.comparingInt(MissionDefinitionEntity::getRequiredLevel).reversed())
                        .thenComparing(MissionDefinitionEntity::getId, Comparator.reverseOrder()))
                .map(m -> new MissionResponse(m.getId(), m.getName(), m.getDescription(), m.getArea().name(), m.getRequiredLevel(), m.getBaseXp(), m.getBaseBits(), m.getEnergyCost(), m.getDurationSeconds()))
                .toList();
    }

    private static int missionNumber(MissionDefinitionEntity mission) {
        String id = mission.getId();
        if (id == null) return -1;
        int separator = id.lastIndexOf('_');
        if (separator < 0 || separator == id.length() - 1) return -1;
        try {
            return Integer.parseInt(id.substring(separator + 1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public GetAvailableMissionsUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final MissionDefinitionRepository missionDefinitionRepository) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
    }
}
