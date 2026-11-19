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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class JoinClanUseCase {
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;
    private final ClanBonusService clanBonusService;

    @Transactional
    public ClanResponse execute(String token, UUID clanId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getClanId() != null) {
            throw new BadRequestException("You are already in a clan");
        }
        Clan clan = clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clan not found"));
        if (!clan.isActive()) {
            throw new NotFoundException("Clan not found");
        }
        long memberCount = playerRepository.countByClanId(clan.getId());
        if (memberCount >= clanBonusService.getEffectiveMaxMembers(clan)) {
            throw new BadRequestException("Clan is full");
        }
        player.setClanId(clan.getId());
        player.setClanRole(ClanRole.MEMBER);
        player.setClanJoinedAt(LocalDateTime.now());
        playerRepository.save(player);
        return mapper.toResponse(clan, player, playerRepository.findByClanId(clan.getId()));
    }

    public JoinClanUseCase(final ClanRepository clanRepository, final PlayerRepository playerRepository, final ClanResponseMapper mapper, final ClanBonusService clanBonusService) {
        this.clanRepository = clanRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
        this.clanBonusService = clanBonusService;
    }
}
