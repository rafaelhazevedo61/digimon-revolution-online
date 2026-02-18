package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.api.MissionResultResponse;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final AddItemUseCase addItemUseCase;

    public MissionResultResponse execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        Digimon digimon = digimonRepository
                .findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        int previousLevel = digimon.getLevel();

        int xpReward = 120;

        digimon.gainExperience(xpReward);

        digimonRepository.save(digimon);

        addItemUseCase.execute(playerId, ItemType.TRAINING_STONE, 1);

        return new MissionResultResponse(
                xpReward,
                digimon.getLevel()
        );
    }
}
