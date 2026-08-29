package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Alterna a proteção de um Digimon armazenado contra sacrifício. */
@Service
public class ToggleDigimonLockUseCase {
    private final DigimonRepository digimonRepository;

    @Transactional
    public Digimon execute(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!playerId.equals(digimon.getPlayerId())) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        if (digimon.getStatus() != DigimonStatus.STORED) {
            throw new BadRequestException("Only stored Digimons can be locked");
        }

        digimon.setLocked(!digimon.isLocked());
        return digimonRepository.save(digimon);
    }

    public ToggleDigimonLockUseCase(DigimonRepository digimonRepository) {
        this.digimonRepository = digimonRepository;
    }
}
