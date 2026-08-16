package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClanExperienceService {

    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void grantExperience(UUID playerId, int baseXp) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null || player.getClanId() == null) {
            return;
        }
        Clan clan = clanRepository.findById(player.getClanId()).orElse(null);
        if (clan == null) {
            return;
        }
        ClanRules.addExperience(clan, baseXp);
        clanRepository.save(clan);
    }
}
