package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class RetrieveDigimonUseCase {
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public Digimon execute(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findById(digimonId).orElseThrow(() -> new NotFoundException("Digimon not found"));
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        if (digimon.getStatus() != DigimonStatus.STORED) {
            throw new BadRequestException("Digimon is not in storage");
        }
        long activeCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.ACTIVE);
        if (activeCount >= player.getMaxDigimonSlots()) {
            throw new BadRequestException("Slots ativos cheios (" + activeCount + "/" + player.getMaxDigimonSlots() + "). Guarde um Digimon primeiro.");
        }
        digimon.setStatus(DigimonStatus.ACTIVE);
        digimonRepository.save(digimon);
        return digimon;
    }

    public RetrieveDigimonUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
    }
}
