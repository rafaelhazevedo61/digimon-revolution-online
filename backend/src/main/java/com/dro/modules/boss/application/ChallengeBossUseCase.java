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
import com.dro.modules.equipment.domain.EquipmentRarityRules;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final AddItemUseCase addItemUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;

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
        validateCooldown(playerId, boss);

        digimon.regenerateEnergy();
        if (digimon.getEnergy() < boss.getEnergyCost()) {
            throw new BadRequestException("Not enough energy. Required: " + boss.getEnergyCost() + ", current: " + digimon.getEnergy());
        }

        digimon.consumeEnergy(boss.getEnergyCost());

        List<Equipment> equippedItems = equipmentRepository.findByDigimonId(digimon.getId())
                .stream().filter(Equipment::isEquipped).toList();

        int totalHp = digimon.getHp() + EquipmentRules.totalBonusHp(equippedItems);
        int totalAtk = digimon.getAttack() + EquipmentRules.totalBonusAttack(equippedItems);
        int totalDef = digimon.getDefense() + EquipmentRules.totalBonusDefense(equippedItems);

        double digimonPower = BossCombatRules.calculatePower(totalHp, totalAtk, totalDef);
        double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());
        int winChance = BossCombatRules.calculateWinChance(digimonPower, bossPower);

        boolean victory;
        if (BossCombatRules.isBelowThreshold(winChance)) {
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
            bitsGained = boss.getBaseBitsReward();
            drops = rollDrops(boss, digimon.getId());
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

    private List<DropRewardResponse> rollDrops(BossDefinitionEntity boss, UUID digimonId) {
        List<DropRewardResponse> rewards = new ArrayList<>();

        if (boss.getDrops() == null) return rewards;

        for (BossDropEntity drop : boss.getDrops()) {
            int roll = ThreadLocalRandom.current().nextInt(1, 101);
            if (roll > drop.getChance()) continue;

            int quantity = drop.getMinQuantity();
            if (drop.getMaxQuantity() > drop.getMinQuantity()) {
                quantity = ThreadLocalRandom.current().nextInt(drop.getMinQuantity(), drop.getMaxQuantity() + 1);
            }

            if ("EQUIPMENT_POOL".equals(drop.getDropType())) {
                List<EquipmentTemplateEntity> templates = equipmentTemplateRepository.findByActiveTrueOrderByNameAsc();
                if (!templates.isEmpty()) {
                    EquipmentTemplateEntity picked = templates.get(
                            ThreadLocalRandom.current().nextInt(templates.size()));
                    String profile = "BOSS_" + boss.getBossType().name();
                    EquipmentRarity rarity = EquipmentRarityRules.rollRarity(profile);
                    grantEquipmentUseCase.execute(digimonId, picked.getName(), rarity);
                    rewards.add(new DropRewardResponse("EQUIPMENT", picked.getName(), picked.getName(), 1, rarity.name()));
                }
            } else if ("EQUIPMENT".equals(drop.getDropType())) {
                if (drop.getTemplateName() != null) {
                    EquipmentRarity rarity;
                    if (drop.getEquipmentRarity() != null) {
                        rarity = EquipmentRarity.valueOf(drop.getEquipmentRarity());
                    } else {
                        String profile = "BOSS_" + boss.getBossType().name();
                        rarity = EquipmentRarityRules.rollRarity(profile);
                    }
                    grantEquipmentUseCase.execute(digimonId, drop.getTemplateName(), rarity);
                    rewards.add(new DropRewardResponse("EQUIPMENT", drop.getTemplateName(), drop.getTemplateName(), 1, rarity.name()));
                }
            } else {
                try {
                    ItemType itemType = ItemType.valueOf(drop.getItemCode());
                    addItemUseCase.execute(digimonId, itemType, quantity);
                    rewards.add(new DropRewardResponse("ITEM", drop.getItemCode(), drop.getItemCode(), quantity, null));
                } catch (IllegalArgumentException ignored) {
                    // Skip unknown item codes
                }
            }
        }

        return rewards;
    }
}
