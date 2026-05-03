package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.response.PlayerStartupResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.enums.StartupDestination;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerStartupUseCase {

    private final PlayerRepository playerRepository;

    public PlayerStartupResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        boolean hasSelectedStarter = player.isStarterSelected() && player.getSelectedDigitama() == null;

        StartupDestination redirectTo = null;

        if(player.isStarterSelected() && player.getSelectedDigitama() != null) {
            redirectTo = StartupDestination.DIGITAMA_HATCHING;
        }

        if(player.isStarterSelected() && player.getSelectedDigitama() == null) {
            redirectTo = StartupDestination.DIGIMON_SELECTION;
        }

        if(!player.isStarterSelected()) {
            redirectTo = StartupDestination.DIGITAMA_SELECTION;
        }

        return new PlayerStartupResponse(
                hasSelectedStarter,
                redirectTo
        );
    }
}