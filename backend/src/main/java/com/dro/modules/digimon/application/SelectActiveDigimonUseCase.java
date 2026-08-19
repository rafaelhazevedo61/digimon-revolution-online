package com.dro.modules.digimon.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
@RequiredArgsConstructor
public class SelectActiveDigimonUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    @Transactional
    public void execute(String token, UUID digimonId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("Digimon does not belong to player");
        }

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        player.setActiveDigimonId(digimonId);
        playerRepository.save(player);
    }
}
