package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
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
public class AddExperienceUseCase {

    private final DigimonRepository repository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token, int xp) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = repository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        digimon.gainExperience(xp);

        repository.save(digimon);
    }

    @Transactional
    public void executeForDigimon(UUID digimonId, int xp) {
        Digimon digimon = repository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        digimon.gainExperience(xp);
        repository.save(digimon);
    }
}
