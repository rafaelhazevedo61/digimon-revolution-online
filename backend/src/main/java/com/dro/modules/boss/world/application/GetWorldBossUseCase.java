package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.api.dto.response.WorldBossResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Boss Mundial.
 */
@Service
@RequiredArgsConstructor
public class GetWorldBossUseCase {

    private final PlayerRepository playerRepository;
    private final WorldBossService worldBossService;
    private final WorldBossResponseMapper mapper;

    @Transactional
    public WorldBossResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        return mapper.toResponse(worldBossService.getOrCreateToday(), player.getId());
    }
}
