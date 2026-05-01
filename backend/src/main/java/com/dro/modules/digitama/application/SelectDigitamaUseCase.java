package com.dro.modules.digitama.application;

import com.dro.modules.digitama.domain.DigitamaType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelectDigitamaUseCase {

    private final PlayerRepository repository;

    public void execute(String token, DigitamaType type) {

        String playerIdPart = token.split(":")[1];
        UUID playerId = UUID.fromString(playerIdPart);

        Player player = repository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getSelectedDigitama() != null) {
            throw new ConflictException("Digitama already selected");
        }

        player.setSelectedDigitama(type);
        repository.save(player);
    }
}
