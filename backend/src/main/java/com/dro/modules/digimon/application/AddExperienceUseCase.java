package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddExperienceUseCase {

    private final DigimonRepository repository;
    private final PlayerRepository playerRepository;

    public void execute(String token, int xp) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        Digimon digimon = repository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        digimon.gainExperience(xp);

        repository.save(digimon);
    }
}
