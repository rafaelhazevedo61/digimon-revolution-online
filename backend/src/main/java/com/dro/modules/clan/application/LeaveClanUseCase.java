package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class LeaveClanUseCase {
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, UUID clanId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Clan clan = clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clan not found"));
        if (player.getClanId() == null || !player.getClanId().equals(clan.getId())) {
            throw new BadRequestException("You are not a member of this clan");
        }
        if (player.getClanRole() == ClanRole.LEADER) {
            throw new BadRequestException("Leader must transfer leadership before leaving");
        }
        player.setClanId(null);
        player.setClanRole(null);
        player.setClanJoinedAt(null);
        playerRepository.save(player);
        return mapper.toResponse(clan, player, playerRepository.findByClanId(clan.getId()));
    }

    public LeaveClanUseCase(final ClanRepository clanRepository, final PlayerRepository playerRepository, final ClanResponseMapper mapper) {
        this.clanRepository = clanRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }
}
