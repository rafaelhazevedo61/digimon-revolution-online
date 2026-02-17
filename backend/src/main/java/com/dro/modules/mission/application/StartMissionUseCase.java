package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.MissionResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final DigimonRepository repository;

    public MissionResultResponse execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Digimon digimon = repository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        int previousLevel = digimon.getLevel();

        int xpReward = 120;

        digimon.gainExperience(xpReward);

        repository.save(digimon);

        return new MissionResultResponse(
                xpReward,
                digimon.getLevel()
        );
    }
}
