package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
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
    private final PlayerRepository playerRepository;

    public List<DigimonResponse> execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        var digimons = digimonRepository.findByPlayerId(playerId);

        return digimons.stream()
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
                        d.getId().equals(player.getActiveDigimonId()),
                        d.getCreatedAt()
                ))
                .toList();
    }
}
