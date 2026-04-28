package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token) {

        UUID playerId = extractPlayerId(token);

        Player player = findPlayer(playerId);

        validateDigitamaSelection(player);

        Digimon digimon = createDigimon(playerId, player);

        digimonRepository.save(digimon);

        setActiveIfFirstDigimon(player, digimon);

        clearSelectedDigitama(player);
    }

    private UUID extractPlayerId(String token) {
        return UUID.fromString(token.split(":")[1]);
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    private void validateDigitamaSelection(Player player) {
        if (player.getSelectedDigitama() == null) {
            throw new RuntimeException("Digitama already hatched or not selected");
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