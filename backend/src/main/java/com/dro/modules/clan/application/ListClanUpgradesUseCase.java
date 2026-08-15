package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListClanUpgradesUseCase {

    private final ClanBonusService clanBonusService;
    private final com.dro.modules.clan.infra.ClanRepository clanRepository;

    public List<ClanUpgradeResponse> execute(String token, UUID clanId) {
        TokenExtractor.extractPlayerId(token);
        if (!clanRepository.existsById(clanId)) {
            throw new NotFoundException("Clan not found");
        }
        return clanBonusService.listUpgrades(clanId);
    }
}
