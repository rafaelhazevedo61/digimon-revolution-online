package com.dro.modules.player.application;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
public class GetPlayerUseCase {
    private final PlayerRepository repository;

    public PlayerResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = repository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        return new PlayerResponse(player.getId(), player.getUsername(), player.getEmail(), player.getCreatedAt());
    }

    public GetPlayerUseCase(final PlayerRepository repository) {
        this.repository = repository;
    }
}
