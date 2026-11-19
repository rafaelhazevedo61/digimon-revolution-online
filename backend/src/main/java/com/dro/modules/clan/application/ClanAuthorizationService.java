package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de serviço de aplicação do módulo de Clãs.
 */
@Service
public class ClanAuthorizationService {
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;

    public Player getPlayer(UUID playerId) {
        return playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
    }

    public Clan getClan(UUID clanId) {
        return clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clan not found"));
    }

    public Player getMember(UUID clanId, String username) {
        Player member = playerRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Player not found"));
        if (member.getClanId() == null || !member.getClanId().equals(clanId)) {
            throw new NotFoundException("Player is not a member of this clan");
        }
        return member;
    }

    public boolean isAdmin(Player player) {
        return player.getUserType() == UserType.ADMIN;
    }

    public void assertInClan(Player player, Clan clan) {
        if (player.getClanId() == null || !player.getClanId().equals(clan.getId())) {
            throw new ForbiddenException("You are not a member of this clan");
        }
    }

    public void assertCanManageInfo(Player player, Clan clan) {
        assertInClan(player, clan);
        if (!isAdmin(player) && !ClanRules.canManageClanInfo(player.getClanRole())) {
            throw new ForbiddenException("You are not allowed to manage this clan");
        }
    }

    public void assertCanInvite(Player player, Clan clan) {
        assertInClan(player, clan);
        if (!isAdmin(player) && !ClanRules.canManageClanInfo(player.getClanRole())) {
            throw new ForbiddenException("You are not allowed to invite players to this clan");
        }
    }

    public void assertCanKick(Player actor, Clan clan, Player target) {
        assertInClan(actor, clan);
        assertInClan(target, clan);
        if (!isAdmin(actor) && !ClanRules.canKick(actor.getClanRole(), target.getClanRole())) {
            throw new ForbiddenException("You are not allowed to kick this member");
        }
    }

    public void assertCanChangeRole(Player actor, Clan clan, ClanRole currentRole, ClanRole newRole) {
        assertInClan(actor, clan);
        if (isAdmin(actor)) return;
        if (newRole == ClanRole.LEADER) {
            throw new ForbiddenException("Use the transfer endpoint to change the leader");
        }
        if (!ClanRules.canPromote(actor.getClanRole(), currentRole) && !ClanRules.canDemote(actor.getClanRole(), currentRole)) {
            throw new ForbiddenException("You are not allowed to change this member\'s role");
        }
        if (currentRole == ClanRole.LEADER) {
            throw new ForbiddenException("Cannot change the leader\'s role directly");
        }
    }

    public void assertCanTransferLeadership(Player actor, Clan clan, Player target) {
        assertInClan(actor, clan);
        assertInClan(target, clan);
        if (!isAdmin(actor) && !ClanRules.canTransferLeadership(actor.getClanRole())) {
            throw new ForbiddenException("Only the leader can transfer leadership");
        }
    }

    public void assertCanDissolve(Player player, Clan clan) {
        assertInClan(player, clan);
        if (!isAdmin(player) && !ClanRules.canDissolve(player.getClanRole())) {
            throw new ForbiddenException("Only the leader can dissolve the clan");
        }
    }

    public void assertCanBuyUpgrade(Player player, Clan clan) {
        assertInClan(player, clan);
        if (!isAdmin(player) && player.getClanRole() != ClanRole.LEADER) {
            throw new ForbiddenException("Only the leader can buy upgrades");
        }
    }

    public void assertCanDepositStorage(Player player, Clan clan) {
        assertInClan(player, clan);
    }

    public void assertCanWithdrawStorage(Player player, Clan clan) {
        assertInClan(player, clan);
        if (!isAdmin(player) && player.getClanRole() != ClanRole.LEADER && player.getClanRole() != ClanRole.OFFICER) {
            throw new ForbiddenException("Only clan officers and leaders can withdraw items");
        }
    }

    public List<Player> getMembers(UUID clanId) {
        return playerRepository.findByClanId(clanId);
    }

    public ClanAuthorizationService(final ClanRepository clanRepository, final PlayerRepository playerRepository) {
        this.clanRepository = clanRepository;
        this.playerRepository = playerRepository;
    }
}
