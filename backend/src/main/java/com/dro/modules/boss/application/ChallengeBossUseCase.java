package com.dro.modules.boss.application;

import com.dro.modules.boss.api.dto.response.BossChallengeResponse;
import com.dro.modules.boss.api.dto.response.DropRewardResponse;
import com.dro.modules.boss.domain.*;
import com.dro.modules.boss.infra.BossAttemptRepository;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.equipment.domain.EquipmentRarityRules;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeBossUseCase {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final BossAttemptRepository bossAttemptRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final EquipmentRepository equipmentRepository;
    private final AddItemUseCase addItemUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;
    private final ClanBonusService clanBonusService;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final GlobalDamageBuffService globalDamageBuffService;

    @Transactional
    public BossChallengeResponse execute(String token, String bossCode, UUID digimonId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        BossDefinitionEntity boss = bossDefinitionRepository.findByCode(bossCode)
                .orElseThrow(() -> new NotFoundException("Boss not found: " + bossCode));

        if (!boss.isActive()) {
            throw new BadRequestException("Boss is not active");
        }

        if (boss.getBossType() == BossType.DAILY) {
            validateDailyRotation(boss);
        }

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }

        validateRequirements(boss, digimon);

        boolean isAdmin = player.getUserType() == UserType.ADMIN;
        UUID clanId = player.getClanId();

        if (!isAdmin) {
            validateCooldown(playerId, boss);

            int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;
            digimon.regenerateEnergy(maxEnergyBonus);
            int energyCost = clanId != null
                    ? applyCostReduction(boss.getEnergyCost(), clanBonusService.getEnergyCostMultiplier(clanId))
                    : boss.getEnergyCost();
            if (digimon.getEnergy() < energyCost) {
                throw new BadRequestException("Not enough energy. Required: " + energyCost + ", current: " + digimon.getEnergy());
            }

            digimon.consumeEnergy(energyCost);
        }

        List<Equipment> equippedItems = equipmentRepository.findByDigimonId(digimon.getId())
                .stream().filter(Equipment::isEquipped).toList();

        double atkBonus = clanId != null ? clanBonusService.getAttackBonusPercent(clanId) : 0.0;
        double defBonus = clanId != null ? clanBonusService.getDefenseBonusPercent(clanId) : 0.0;
        double hpBonus = clanId != null ? clanBonusService.getHpBonusPercent(clanId) : 0.0;

        int totalHp = applyBonus(digimon.getHp() + EquipmentRules.totalBonusHp(equippedItems), hpBonus);
        int totalAtk = applyBonus(digimon.getAttack() + EquipmentRules.totalBonusAttack(equippedItems), atkBonus);
        int totalDef = applyBonus(digimon.getDefense() + EquipmentRules.totalBonusDefense(equippedItems), defBonus);

        double rawDigimonPower = BossCombatRules.calculatePower(totalHp, totalAtk, totalDef);
        double digimonPower = rawDigimonPower * globalDamageBuffService.getMultiplier();
        double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());

        boolean buffActive = globalDamageBuffService.isEnabled();
        int winChance = buffActive ? 100 : BossCombatRules.calculateWinChance(digimonPower, bossPower);

        boolean victory;
        if (buffActive) {
            victory = true;
        } else if (BossCombatRules.isBelowThreshold(winChance)) {
            victory = false;
        } else {
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            victory = roll <= winChance;
        }

        int xpGained;
        int bitsGained;
        List<DropRewardResponse> drops = new ArrayList<>();

        if (victory) {
            xpGained = boss.getBaseXpReward();
            double bitsMultiplier = clanId != null ? clanBonusService.getMissionBitsMultiplier(clanId) : 1.0;
            bitsGained = (int) Math.floor(boss.getBaseBitsReward() * bitsMultiplier);
            drops = rollDrops(boss, digimon.getId(), clanId);
            if (clanId != null) {
                clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.BOSSES_DEFEATED);
            }
        } else {
            xpGained = (int) Math.round(boss.getBaseXpReward() * boss.getDefeatXpPercent() / 100.0);
            bitsGained = 0;
        }

        digimon.gainExperience(xpGained);
        digimon.setBits(digimon.getBits() + bitsGained);
        digimonRepository.save(digimon);

        BossAttemptEntity attempt = BossAttemptEntity.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .digimonId(digimonId)
                .bossId(boss.getId())
                .status(victory ? BossAttemptStatus.VICTORY : BossAttemptStatus.DEFEAT)
                .damageDealt((int) digimonPower)
                .xpGained(xpGained)
                .bitsGained(bitsGained)
                .createdAt(Instant.now())
                .build();

        bossAttemptRepository.save(attempt);

        return new BossChallengeResponse(
                boss.getCode(),
                boss.getName(),
                victory ? "VICTORY" : "DEFEAT",
                winChance,
                digimonPower,
                bossPower,
                xpGained,
                bitsGained,
                drops
        );
    }

    private void validateDailyRotation(BossDefinitionEntity boss) {
        List<BossDefinitionEntity> dailySameStage = bossDefinitionRepository.findAllActive().stream()
                .filter(b -> b.getBossType() == BossType.DAILY && b.getRequiredStage() == boss.getRequiredStage())
                .sorted(Comparator.comparingLong(BossDefinitionEntity::getId))
                .toList();

        if (dailySameStage.size() <= 1) return;

        long dayIndex = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        int todayIndex = (int) (dayIndex % dailySameStage.size());
        BossDefinitionEntity todaysBoss = dailySameStage.get(todayIndex);

        if (!todaysBoss.getId().equals(boss.getId())) {
            throw new BadRequestException("This daily boss is not available today. Today's boss: " + todaysBoss.getName());
        }
    }

    private void validateRequirements(BossDefinitionEntity boss, Digimon digimon) {
        if (digimon.getStage().ordinal() < boss.getRequiredStage().ordinal()) {
            throw new BadRequestException("Digimon stage too low. Required: " + boss.getRequiredStage());
        }
        if (digimon.getLevel() < boss.getRequiredLevel()) {
            throw new BadRequestException("Digimon level too low. Required: " + boss.getRequiredLevel());
        }
        if (digimon.getRebirthCount() < boss.getRequiredRebirths()) {
            throw new BadRequestException("Not enough rebirths. Required: " + boss.getRequiredRebirths());
        }
    }

    private void validateCooldown(UUID playerId, BossDefinitionEntity boss) {
        var lastAttempt = bossAttemptRepository
                .findFirstByPlayerIdAndBossIdOrderByCreatedAtDesc(playerId, boss.getId());

        if (lastAttempt.isEmpty()) return;

        Instant lastTime = lastAttempt.get().getCreatedAt();
        Instant now = Instant.now();

        long cooldownMinutes = boss.getCooldownMinutes();
        Instant cooldownEnd = lastTime.plus(cooldownMinutes, ChronoUnit.MINUTES);

        if (now.isBefore(cooldownEnd)) {
            long remainingSeconds = now.until(cooldownEnd, ChronoUnit.SECONDS);
            throw new BadRequestException("Boss on cooldown. " + remainingSeconds + " seconds remaining.");
        }
    }

    private List<DropRewardResponse> rollDrops(BossDefinitionEntity boss, UUID digimonId, UUID clanId) {
        List<DropRewardResponse> rewards = new ArrayList<>();

        if (boss.getDrops() == null) return rewards;

        double dropBonusPercent = clanId != null ? clanBonusService.getBossDropBonusPercent(clanId) : 0.0;
        int dropBonusPoints = (int) Math.round(dropBonusPercent * 100);

        List<BossDropEntity> equipDrops = new ArrayList<>();
        List<BossDropEntity> itemDrops = new ArrayList<>();

        for (BossDropEntity drop : boss.getDrops()) {
            if ("EQUIPMENT".equals(drop.getDropType())) {
                equipDrops.add(drop);
            } else {
                itemDrops.add(drop);
            }
        }

        if (!equipDrops.isEmpty()) {
            int poolChance = Math.min(100, equipDrops.get(0).getChance() + dropBonusPoints);
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            if (roll <= poolChance) {
                BossDropEntity picked = equipDrops.get(
                        ThreadLocalRandom.current().nextInt(equipDrops.size()));
                String profile = "BOSS_" + boss.getBossType().name();
                EquipmentRarity rarity = EquipmentRarityRules.rollRarity(profile, dropBonusPercent);
                grantEquipmentUseCase.execute(digimonId, picked.getTemplateName(), rarity);
                rewards.add(new DropRewardResponse("EQUIPMENT", picked.getTemplateName(), picked.getTemplateName(), 1, rarity.name()));
            }
        }

        for (BossDropEntity drop : itemDrops) {
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            if (roll > Math.min(100, drop.getChance() + dropBonusPoints)) continue;

            int quantity = drop.getMinQuantity();
            if (drop.getMaxQuantity() > drop.getMinQuantity()) {
                quantity = ThreadLocalRandom.current().nextInt(drop.getMinQuantity(), drop.getMaxQuantity() + 1);
            }

            try {
                ItemType itemType = ItemType.valueOf(drop.getItemCode());
                addItemUseCase.execute(digimonId, itemType, quantity);
                rewards.add(new DropRewardResponse("ITEM", drop.getItemCode(), drop.getItemCode(), quantity, null));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return rewards;
    }

    private int applyBonus(int base, double percent) {
        if (percent <= 0) return base;
        return (int) Math.floor(base * (1.0 + percent));
    }

    private int applyCostReduction(int baseCost, double multiplier) {
        if (multiplier >= 1.0) return baseCost;
        return Math.max(1, (int) Math.floor(baseCost * multiplier));
    }
}
