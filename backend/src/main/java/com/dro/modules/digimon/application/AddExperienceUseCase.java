package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddExperienceUseCase {

    private final DigimonRepository repository;

    public void execute(String token, int xp) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Digimon digimon = repository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        digimon.gainExperience(xp);

        repository.save(digimon);
    }
}
