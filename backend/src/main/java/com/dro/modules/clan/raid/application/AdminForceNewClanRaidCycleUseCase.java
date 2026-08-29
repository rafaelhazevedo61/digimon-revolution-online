package com.dro.modules.clan.raid.application;

import com.dro.modules.clan.raid.domain.ClanRaid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Abre manualmente um novo ciclo da incursão de um clã após a derrota do ciclo atual. */
@Service
public class AdminForceNewClanRaidCycleUseCase {
    private final ClanRaidService clanRaidService;

    @Transactional
    public ClanRaid execute(UUID clanId) {
        return clanRaidService.forceNewCycle(clanId);
    }

    public AdminForceNewClanRaidCycleUseCase(final ClanRaidService clanRaidService) {
        this.clanRaidService = clanRaidService;
    }
}
