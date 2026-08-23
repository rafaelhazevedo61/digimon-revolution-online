package com.dro.modules.player.application;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
@RequiredArgsConstructor
public class RevokePlayerSessionsUseCase {

    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token) {
        Player player = playerRepository.findById(TokenExtractor.extractPlayerId(token))
                .orElseThrow(() -> new NotFoundException("Player not found"));

        player.incrementTokenVersion();
        playerRepository.save(player);
    }
}
