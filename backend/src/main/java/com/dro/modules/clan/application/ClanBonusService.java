package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanUpgradePurchase;
import com.dro.modules.clan.domain.ClanUpgradeType;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradePurchaseRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Componente da camada de serviço de aplicação do módulo de Clãs.
 */
@Service
public class ClanBonusService {
    private static final String ATTACK_BONUS_CODE = "ATTACK_BONUS";
    private static final String DEFENSE_BONUS_CODE = "DEFENSE_BONUS";
    private static final String HP_BONUS_CODE = "HP_BONUS";
    private static final String HONOR_MARKS_BONUS_CODE = "HONOR_MARKS_BONUS";
    private static final String MAX_ENERGY_BONUS_CODE = "MAX_ENERGY_BONUS";
    private static final String MISSION_BITS_BONUS_CODE = "MISSION_BITS_BONUS";
    private static final String MISSION_XP_BONUS_CODE = "MISSION_XP_BONUS";
    private static final String ENERGY_COST_REDUCTION_CODE = "ENERGY_COST_REDUCTION";
    private static final String BOSS_DROP_BONUS_CODE = "BOSS_DROP_BONUS";
    private static final String MEMBER_CAPACITY_CODE = "MEMBER_CAPACITY";
    private final ClanRepository clanRepository;
    private final ClanUpgradeTypeRepository upgradeTypeRepository;
    private final ClanUpgradePurchaseRepository upgradePurchaseRepository;

    public List<ClanUpgradeResponse> listUpgrades(UUID clanId) {
        Clan clan = clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clan not found"));
        List<ClanUpgradeType> allTypes = upgradeTypeRepository.findAll();
        Map<String, Integer> purchaseLevels = getPurchaseLevels(clanId);
        return allTypes.stream().sorted(Comparator.comparingInt(ClanUpgradeType::getUnlockedAtClanLevel).thenComparing(ClanUpgradeType::getCode)).map(t -> toResponse(t, clan, purchaseLevels.getOrDefault(t.getCode(), 0))).toList();
    }

    public int getUpgradeLevel(UUID clanId, String code) {
        return upgradePurchaseRepository.findByClanIdAndUpgradeCode(clanId, code).map(ClanUpgradePurchase::getLevel).orElse(0);
    }

    public int getMemberCapacityBonus(UUID clanId) {
        return getUpgradeLevel(clanId, MEMBER_CAPACITY_CODE);
    }

    public int getEffectiveMaxMembers(Clan clan) {
        return clan.getMaxMembers() + getMemberCapacityBonus(clan.getId());
    }

    public double getAttackBonusPercent(UUID clanId) {
        return getUpgradeLevel(clanId, ATTACK_BONUS_CODE) * getEffectPerLevel(ATTACK_BONUS_CODE);
    }

    public double getDefenseBonusPercent(UUID clanId) {
        return getUpgradeLevel(clanId, DEFENSE_BONUS_CODE) * getEffectPerLevel(DEFENSE_BONUS_CODE);
    }

    public double getHpBonusPercent(UUID clanId) {
        return getUpgradeLevel(clanId, HP_BONUS_CODE) * getEffectPerLevel(HP_BONUS_CODE);
    }

    public double getHonorMarksBonusPercent(UUID clanId) {
        return getUpgradeLevel(clanId, HONOR_MARKS_BONUS_CODE) * getEffectPerLevel(HONOR_MARKS_BONUS_CODE);
    }

    public int getMaxEnergyBonus(UUID clanId) {
        return getUpgradeLevel(clanId, MAX_ENERGY_BONUS_CODE) * getEffectPerLevelAsInt(MAX_ENERGY_BONUS_CODE);
    }

    public double getMissionBitsMultiplier(UUID clanId) {
        return 1.0 + getUpgradeLevel(clanId, MISSION_BITS_BONUS_CODE) * getEffectPerLevel(MISSION_BITS_BONUS_CODE);
    }

    public double getMissionXpMultiplier(UUID clanId) {
        return 1.0 + getUpgradeLevel(clanId, MISSION_XP_BONUS_CODE) * getEffectPerLevel(MISSION_XP_BONUS_CODE);
    }

    public double getEnergyCostMultiplier(UUID clanId) {
        int level = getUpgradeLevel(clanId, ENERGY_COST_REDUCTION_CODE);
        double effectPerLevel = getEffectPerLevel(ENERGY_COST_REDUCTION_CODE);
        double reduction = 0.05 + (level * effectPerLevel);
        return Math.max(0.1, 1.0 - reduction);
    }

    public double getBossDropBonusPercent(UUID clanId) {
        return getUpgradeLevel(clanId, BOSS_DROP_BONUS_CODE) * getEffectPerLevel(BOSS_DROP_BONUS_CODE);
    }

    public int calculateNextCost(ClanUpgradeType type, int currentLevel) {
        if (currentLevel >= type.getMaxLevel()) {
            return 0;
        }
        BigDecimal base = BigDecimal.valueOf(type.getBaseHonorMarksCost());
        BigDecimal multiplier = type.getCostMultiplier();
        BigDecimal cost = base.multiply(multiplier.pow(currentLevel));
        return cost.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public int calculateNextCost(String code, int currentLevel) {
        ClanUpgradeType type = upgradeTypeRepository.findById(code).orElseThrow(() -> new NotFoundException("Upgrade not found"));
        return calculateNextCost(type, currentLevel);
    }

    public List<ClanUpgradeResponse> activeUpgrades(UUID clanId) {
        return listUpgrades(clanId).stream().filter(u -> u.currentLevel() > 0).toList();
    }

    private Map<String, Integer> getPurchaseLevels(UUID clanId) {
        Map<String, Integer> map = new HashMap<>();
        for (ClanUpgradePurchase purchase : upgradePurchaseRepository.findByClanId(clanId)) {
            map.put(purchase.getUpgradeCode(), purchase.getLevel());
        }
        return map;
    }

    private ClanUpgradeResponse toResponse(ClanUpgradeType type, Clan clan, int currentLevel) {
        int nextCost = calculateNextCost(type, currentLevel);
        boolean unlocked = clan.getLevel() >= type.getUnlockedAtClanLevel();
        boolean maxed = currentLevel >= type.getMaxLevel();
        double totalEffect = currentLevel * type.getEffectPerLevel().doubleValue();
        return new ClanUpgradeResponse(type.getCode(), type.getName(), type.getDescription(), type.getUnlockedAtClanLevel(), currentLevel, type.getMaxLevel(), maxed ? 0 : nextCost, type.getEffectPerLevel(), totalEffect, unlocked, maxed);
    }

    private double getEffectPerLevel(String code) {
        return upgradeTypeRepository.findById(code).map(ClanUpgradeType::getEffectPerLevel).map(BigDecimal::doubleValue).orElse(0.0);
    }

    private int getEffectPerLevelAsInt(String code) {
        return upgradeTypeRepository.findById(code).map(ClanUpgradeType::getEffectPerLevel).map(b -> b.setScale(0, RoundingMode.HALF_UP).intValue()).orElse(0);
    }

    public ClanBonusService(final ClanRepository clanRepository, final ClanUpgradeTypeRepository upgradeTypeRepository, final ClanUpgradePurchaseRepository upgradePurchaseRepository) {
        this.clanRepository = clanRepository;
        this.upgradeTypeRepository = upgradeTypeRepository;
        this.upgradePurchaseRepository = upgradePurchaseRepository;
    }
}
