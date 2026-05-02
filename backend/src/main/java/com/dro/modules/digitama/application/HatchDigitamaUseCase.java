package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    public Digimon execute(String token) {

        UUID playerId = extractPlayerId(token);

        Player player = findPlayer(playerId);

        validateDigitamaSelection(player);

        Digimon digimon = createDigimon(playerId, player);

        digimonRepository.save(digimon);

        setActiveIfFirstDigimon(player, digimon);

        clearSelectedDigitama(player);

        return digimon;
    }

    private UUID extractPlayerId(String token) {
        return TokenExtractor.extractPlayerId(token);
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
    }

    private void validateDigitamaSelection(Player player) {
        if (player.getSelectedDigitama() == null) {
            throw new BadRequestException("Digitama already hatched or not selected");
        }
    }

    private Digimon createDigimon(UUID playerId, Player player) {
        return DigimonFactory.createBaby(
                playerId,
                player.getSelectedDigitama()
        );
    }

    private void setActiveIfFirstDigimon(Player player, Digimon digimon) {

        if (player.getActiveDigimonId() == null) {
            player.setActiveDigimonId(digimon.getId());
            playerRepository.save(player);
        }
    }

    private void clearSelectedDigitama(Player player) {
        player.setSelectedDigitama(null);
        playerRepository.save(player);
    }
}