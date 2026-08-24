package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class DissolveClanUseCase {
    private final ClanAuthorizationService authorization;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;

    /**
     * Dissolve o clã por exclusão lógica: a linha em {@code clans} é preservada
     * (active = false, dissolved_at preenchido) em vez de removida. Isso evita
     * violação de foreign key em tabelas que referenciam clan_id sem
     * ON DELETE CASCADE (clan_upgrade_purchases, player_clan_missions,
     * clan_raid_instances, clan_invitations) e mantém o histórico do clã.
     */
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
        clan.setActive(false);
        clan.setDissolvedAt(LocalDateTime.now());
        clanRepository.save(clan);
    }

    public DissolveClanUseCase(final ClanAuthorizationService authorization, final ClanRepository clanRepository, final PlayerRepository playerRepository) {
        this.authorization = authorization;
        this.clanRepository = clanRepository;
        this.playerRepository = playerRepository;
    }
}
