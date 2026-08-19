package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.infra.ClanRepository;
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
public class TransferLeadershipUseCase {

    private final ClanAuthorizationService authorization;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, UUID clanId, String username) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player actor = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);
        Player target = authorization.getMember(clanId, username);

        authorization.assertCanTransferLeadership(actor, clan, target);

        actor.setClanRole(ClanRole.OFFICER);
        target.setClanRole(ClanRole.LEADER);
        clan.setLeaderId(target.getId());

        playerRepository.save(actor);
        playerRepository.save(target);
        clanRepository.save(clan);

        return mapper.toResponse(clan, actor, playerRepository.findByClanId(clanId));
    }
}
