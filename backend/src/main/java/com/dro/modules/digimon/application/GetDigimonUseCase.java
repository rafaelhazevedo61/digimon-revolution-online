package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonUseCase {

    private final DigimonRepository repository;

    public DigimonResponse execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Digimon digimon = repository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        return new DigimonResponse(
                digimon.getId(),
                digimon.getName(),
                digimon.getType(),
                digimon.getStage(),
                digimon.getLevel(),
                digimon.getExperience(),
                digimon.getHp(),
                digimon.getAttack(),
                digimon.getDefense(),
                digimon.getIvHp(),
                digimon.getIvAttack(),
                digimon.getIvDefense(),
                digimon.getCreatedAt()
        );
    }
}
