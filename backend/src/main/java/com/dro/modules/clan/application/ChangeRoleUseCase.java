package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.request.ChangeRoleRequest;
import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeRoleUseCase {

    private final ClanAuthorizationService authorization;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, UUID clanId, String username, ChangeRoleRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player actor = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);
        Player target = authorization.getMember(clanId, username);

        ClanRole newRole = request.role();
        ClanRole currentRole = target.getClanRole();

        if (newRole == ClanRole.LEADER) {
            throw new BadRequestException("Use the transfer endpoint to change the leader");
        }
        if (currentRole == ClanRole.LEADER) {
            throw new BadRequestException("Cannot change the leader's role directly");
        }

        authorization.assertCanChangeRole(actor, clan, currentRole, newRole);

        target.setClanRole(newRole);
        playerRepository.save(target);

        return mapper.toResponse(clan, actor, playerRepository.findByClanId(clanId));
    }
}
