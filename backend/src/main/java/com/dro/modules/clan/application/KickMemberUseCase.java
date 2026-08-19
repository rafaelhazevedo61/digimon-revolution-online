package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
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
public class KickMemberUseCase {

    private final ClanAuthorizationService authorization;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, UUID clanId, String username) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player actor = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);
        Player target = authorization.getMember(clanId, username);

        authorization.assertCanKick(actor, clan, target);

        target.setClanId(null);
        target.setClanRole(null);
        target.setClanJoinedAt(null);
        playerRepository.save(target);

        return mapper.toResponse(clan, actor, playerRepository.findByClanId(clanId));
    }
}
