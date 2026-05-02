package com.dro.modules.mission.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionResponse;
import com.dro.modules.mission.domain.MissionCatalog;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAvailableMissionsUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    public List<MissionResponse> execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        var digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        int level = digimon.getLevel();

        return MissionCatalog.MISSIONS.stream()
                .filter(m -> level >= m.getRequiredLevel())
                .map(m -> new MissionResponse(
                        m.getId(),
                        m.getName(),
                        m.getDescription(),
                        m.getRequiredLevel(),
                        m.getBaseXp(),
                        m.getEnergyCost()
                ))
                .toList();
    }
}
