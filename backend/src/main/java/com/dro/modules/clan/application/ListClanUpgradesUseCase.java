package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
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

    public ListClanUpgradesUseCase(final ClanBonusService clanBonusService, final com.dro.modules.clan.infra.ClanRepository clanRepository) {
        this.clanBonusService = clanBonusService;
        this.clanRepository = clanRepository;
    }
}
