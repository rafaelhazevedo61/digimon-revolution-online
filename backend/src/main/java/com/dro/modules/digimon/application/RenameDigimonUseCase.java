package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RenameDigimonUseCase {

    private final DigimonRepository digimonRepository;

    public void execute(String token, UUID digimonId, String newName) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("Digimon does not belong to this player");
        }

        digimon.setName(newName.trim());
        digimonRepository.save(digimon);
    }
}
