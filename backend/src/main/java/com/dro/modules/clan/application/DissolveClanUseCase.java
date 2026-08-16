package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DissolveClanUseCase {

    private final ClanAuthorizationService authorization;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token, UUID clanId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);

        authorization.assertCanDissolve(player, clan);

        List<Player> members = playerRepository.findByClanId(clanId);
        for (Player member : members) {
            member.setClanId(null);
            member.setClanRole(null);
            member.setClanJoinedAt(null);
        }
        playerRepository.saveAll(members);
        clanRepository.delete(clan);
    }
}
