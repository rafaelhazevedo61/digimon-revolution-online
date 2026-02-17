package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.api.MissionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final DigimonRepository repository;
    private final AddItemUseCase addItemUseCase;

    public MissionResultResponse execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Digimon digimon = repository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        int previousLevel = digimon.getLevel();

        int xpReward = 120;

        digimon.gainExperience(xpReward);

        repository.save(digimon);

        addItemUseCase.execute(playerId, ItemType.TRAINING_STONE, 1);

        return new MissionResultResponse(
                xpReward,
                digimon.getLevel()
        );
    }
}
