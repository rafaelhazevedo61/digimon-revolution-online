package com.dro.modules.clan.raid.application;

import com.dro.modules.clan.raid.api.dto.response.ClanRaidResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
@RequiredArgsConstructor
public class GetClanRaidUseCase {

    private final PlayerRepository playerRepository;
    private final ClanRaidService clanRaidService;
    private final ClanRaidResponseMapper mapper;

    @Transactional
    public ClanRaidResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() == null) {
            throw new BadRequestException("You must be in a clan to access the raid");
        }

        return mapper.toResponse(clanRaidService.getOrCreateToday(player.getClanId()), playerId);
    }
}
