package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanUpgradePurchase;
import com.dro.modules.clan.domain.ClanUpgradeType;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradePurchaseRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class BuyUpgradeUseCase {
    private final ClanRepository clanRepository;
    private final ClanUpgradeTypeRepository upgradeTypeRepository;
    private final ClanUpgradePurchaseRepository upgradePurchaseRepository;
    private final ClanBonusService clanBonusService;
    private final PlayerRepository playerRepository;

    @Transactional
    public ClanUpgradeResponse execute(String token, UUID clanId, String upgradeCode) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = findPlayer(playerId);
        if (player.getClanId() == null || !player.getClanId().equals(clanId)) {
            throw new ForbiddenException("You are not a member of this clan");
        }
        if (player.getClanRole() != com.dro.modules.clan.domain.ClanRole.LEADER) {
            throw new ForbiddenException("Only the clan leader can buy upgrades");
        }
        Clan clan = clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clan not found"));
        ClanUpgradeType type = upgradeTypeRepository.findById(upgradeCode).orElseThrow(() -> new NotFoundException("Upgrade not found"));
        if (clan.getLevel() < type.getUnlockedAtClanLevel()) {
            throw new BadRequestException("Clan level too low to unlock this upgrade");
        }
        ClanUpgradePurchase purchase = upgradePurchaseRepository.findByClanIdAndUpgradeCode(clanId, upgradeCode).orElseGet(() -> createEmptyPurchase(clanId, upgradeCode));
        if (purchase.getLevel() >= type.getMaxLevel()) {
            throw new BadRequestException("Upgrade already at max level");
        }
        int cost = clanBonusService.calculateNextCost(type, purchase.getLevel());
        if (clan.getHonorMarks() < cost) {
            throw new BadRequestException("Not enough Honor Marks");
        }
        clan.setHonorMarks(clan.getHonorMarks() - cost);
        purchase.setLevel(purchase.getLevel() + 1);
        purchase.setTotalSpentHonorMarks(purchase.getTotalSpentHonorMarks() + cost);
        clanRepository.save(clan);
        upgradePurchaseRepository.save(purchase);
        return clanBonusService.listUpgrades(clanId).stream().filter(u -> u.code().equals(upgradeCode)).findFirst().orElseThrow(() -> new IllegalStateException("Upgrade response not found"));
    }

    private ClanUpgradePurchase createEmptyPurchase(UUID clanId, String upgradeCode) {
        return ClanUpgradePurchase.builder().id(UUID.randomUUID()).clanId(clanId).upgradeCode(upgradeCode).level(0).totalSpentHonorMarks(0).build();
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
    }

    public BuyUpgradeUseCase(final ClanRepository clanRepository, final ClanUpgradeTypeRepository upgradeTypeRepository, final ClanUpgradePurchaseRepository upgradePurchaseRepository, final ClanBonusService clanBonusService, final PlayerRepository playerRepository) {
        this.clanRepository = clanRepository;
        this.upgradeTypeRepository = upgradeTypeRepository;
        this.upgradePurchaseRepository = upgradePurchaseRepository;
        this.clanBonusService = clanBonusService;
        this.playerRepository = playerRepository;
    }
}
