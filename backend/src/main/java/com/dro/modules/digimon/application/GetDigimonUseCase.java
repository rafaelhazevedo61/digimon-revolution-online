package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonUseCase {

    private final DigimonRepository digimonRepository;

    public List<DigimonResponse> execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var digimons = digimonRepository.findByPlayerId(playerId);

        return digimons.stream()
                .filter(d -> d.getStatus() == DigimonStatus.ACTIVE)
                .map(d -> new DigimonResponse(
                        d.getId(),
                        d.getName(),
                        d.getType(),
                        d.getStage(),
                        d.getLevel(),
                        d.getExperience(),
                        d.getHp(),
                        d.getAttack(),
                        d.getDefense(),
                        d.getIvHp(),
                        d.getIvAttack(),
                        d.getIvDefense(),
                        d.getRarity(),
                        d.getPersonality(),
                        d.getEnergy(),
                        d.getMaxEnergy(),
                        d.getBits(),
                        d.getRebirthCount(),
                        d.getStatus()
                ))
                .toList();
    }
}
