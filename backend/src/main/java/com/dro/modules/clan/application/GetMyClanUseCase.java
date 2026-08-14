package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyClanUseCase {

    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    public ClanResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player viewer = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (viewer.getClanId() == null) {
            throw new NotFoundException("You are not in a clan");
        }

        Clan clan = clanRepository.findById(viewer.getClanId())
                .orElseThrow(() -> new NotFoundException("Clan not found"));

        return mapper.toResponse(clan, viewer, playerRepository.findByClanId(clan.getId()));
    }
}
