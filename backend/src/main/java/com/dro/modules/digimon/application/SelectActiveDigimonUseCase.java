package com.dro.modules.digimon.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelectActiveDigimonUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token, UUID digimonId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Digimon does not belong to player");
        }

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setActiveDigimonId(digimonId);
        playerRepository.save(player);
    }
}
