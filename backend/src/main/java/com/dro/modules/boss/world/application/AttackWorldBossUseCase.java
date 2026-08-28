package com.dro.modules.boss.world.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.api.dto.response.AttackWorldBossResponse;
import com.dro.modules.activitycalendar.application.ActivityCalendarService;
import com.dro.modules.activitycalendar.domain.ActivitySource;
import com.dro.modules.boss.world.api.dto.response.WorldBossRewardResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossRules;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import com.dro.shared.gameplay.WeekendDoubleRewardRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Boss Mundial.
 */
@Service
public class AttackWorldBossUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final WorldBossAttackRepository worldBossAttackRepository;
    private final WorldBossService worldBossService;
    private final WorldBossRewardService worldBossRewardService;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;
    private final GlobalDamageBuffService globalDamageBuffService;
    private final TransactionAuditPublisher transactionAuditPublisher;
    private final GameplayConfig gameplayConfig;
    private final ActivityCalendarService activityCalendarService;

    @Transactional
    public AttackWorldBossResponse execute(String token, String idempotencyKey) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active Digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active Digimon not found"));
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        WorldBossInstance instance = worldBossService.getOrCreateToday();
        BossDefinitionEntity boss = bossDefinitionRepository.findById(instance.getBossId()).orElseThrow(() -> new NotFoundException("Boss not found"));
        String requestId = normalizeRequestId(idempotencyKey);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            WorldBossAttack existing = worldBossAttackRepository.findByWorldBossIdAndPlayerIdAndRequestId(instance.getId(), playerId, requestId).orElse(null);
            if (existing != null) {
                return toResponse(boss, instance, existing, worldBossRewardService.findBySourceAttackId(existing.getId()));
            }
        }
        if (instance.getStatus() == WorldBossStatus.DEFEATED || instance.getRemainingHp() <= 0) {
            throw new BadRequestException("The world boss has already been defeated today");
        }
        validateRequirements(boss, digimon);
        WorldBossAttack lastAttack = worldBossAttackRepository.findFirstByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(instance.getId(), playerId).orElse(null);
        int cooldownMinutes = WorldBossRules.attackCooldownMinutes(boss.getCooldownMinutes());
        if (gameplayConfig.isWorldBossCooldownEnabled() && lastAttack != null && lastAttack.getCreatedAt() != null) {
            Instant nextAttackAt = lastAttack.getCreatedAt().plus(Duration.ofMinutes(cooldownMinutes));
            Instant now = Instant.now();
            if (now.isBefore(nextAttackAt)) {
                long remainingSeconds = Math.max(1, Duration.between(now, nextAttackAt).toSeconds());
                long remainingMinutes = (remainingSeconds + 59) / 60;
                throw new BadRequestException("World boss attack cooldown active. Try again in " + remainingMinutes + " minute(s).");
            }
        }
        UUID clanId = player.getClanId();
        int energyCost = 0;
        if (gameplayConfig.isEnergyConsumptionEnabled()) {
            int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;
            digimon.regenerateEnergy(maxEnergyBonus);
            int baseEnergyCost = boss.getEnergyCost();
            energyCost = applyCostReduction(baseEnergyCost, clanId != null ? clanBonusService.getEnergyCostMultiplier(clanId) : 1.0);
            if (digimon.getEnergy() < energyCost) {
                throw new BadRequestException("Not enough energy. Required: " + energyCost + ", current: " + digimon.getEnergy());
            }
            digimon.consumeEnergy(energyCost);
        }
        double digimonPower = digimonPowerService.calculatePower(digimon, clanId);
        double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());
        int winChance = WorldBossRules.calculateWinChance(digimonPower, bossPower);
        int damage = (int) Math.round(WorldBossRules.calculateDamage(instance.getMaxHp(), winChance) * globalDamageBuffService.getMultiplier());
        int actualDamage = Math.min(damage, instance.getRemainingHp());
        Instant rewardTime = Instant.now();
        int xpGained = WeekendDoubleRewardRules.multiplyXp(WorldBossRules.hitXp(boss.getBaseXpReward(), boss.getDefeatXpPercent()), rewardTime);
        int bitsGained = WeekendDoubleRewardRules.multiplyBits(WorldBossRules.hitBits(boss.getBaseBitsReward(), boss.getDefeatXpPercent()), rewardTime);
        digimon.gainExperience(xpGained);
        digimon.setBits(digimon.getBits() + bitsGained);
        instance.setRemainingHp(instance.getRemainingHp() - actualDamage);
        instance.setUpdatedAt(Instant.now());
        boolean defeated = false;
        int defeatedRewardXp = 0;
        int defeatedRewardBits = 0;
        if (instance.getRemainingHp() <= 0) {
            defeated = true;
            instance.setStatus(WorldBossStatus.DEFEATED);
            instance.setDefeatedAt(Instant.now());
            defeatedRewardXp = WeekendDoubleRewardRules.multiplyXp(boss.getBaseXpReward(), rewardTime);
            defeatedRewardBits = WeekendDoubleRewardRules.multiplyBits(boss.getBaseBitsReward(), rewardTime);
            digimon.gainExperience(defeatedRewardXp);
            digimon.setBits(digimon.getBits() + defeatedRewardBits);
        }
        WorldBossAttack attack = WorldBossAttack.builder().id(UUID.randomUUID()).worldBossId(instance.getId()).playerId(playerId).digimonId(digimon.getId()).damage(actualDamage).energyCost(energyCost).bitsGained(bitsGained + defeatedRewardBits).xpGained(xpGained + defeatedRewardXp).createdAt(Instant.now()).build();
        attack.setRequestId(requestId);
        attack.setRemainingHpAfter(instance.getRemainingHp());
        attack.setWinChance(winChance);
        attack.setDefeated(defeated);
        attack.setDefeatedRewardXp(defeatedRewardXp);
        attack.setDefeatedRewardBits(defeatedRewardBits);
        digimonRepository.save(digimon);
        worldBossInstanceRepository.save(instance);
        worldBossAttackRepository.save(attack);
        if (activityCalendarService != null) activityCalendarService.recordActivity(playerId, ActivitySource.WORLD_BOSS_ATTACK, attack.getId().toString());
        List<WorldBossRewardResponse> rewards = worldBossRewardService.grant(boss, instance, attack, defeated);
        transactionAuditPublisher.success("world-boss-attack:" + attack.getId(), "WORLD_BOSS_ATTACKED", "WorldBossAttack", attack.getId().toString(), buildAuditPayload(playerId, boss, instance, attack, rewards));
        return toResponse(boss, instance, attack, rewards);
    }

    private AttackWorldBossResponse toResponse(BossDefinitionEntity boss, WorldBossInstance instance, WorldBossAttack attack, List<WorldBossRewardResponse> rewards) {
        int hitXp = Math.max(0, attack.getXpGained() - attack.getDefeatedRewardXp());
        int hitBits = Math.max(0, attack.getBitsGained() - attack.getDefeatedRewardBits());
        return new AttackWorldBossResponse(instance.getId(), boss.getCode(), boss.getName(), attack.getDamage(), attack.getRemainingHpAfter(), instance.getMaxHp(), attack.isDefeated(), attack.getWinChance(), hitXp, hitBits, attack.getDefeatedRewardXp(), attack.getDefeatedRewardBits(), rewards);
    }

    private String normalizeRequestId(String idempotencyKey) {
        String normalized = idempotencyKey == null || idempotencyKey.isBlank() ? UUID.randomUUID().toString() : idempotencyKey.trim();
        if (normalized.length() > 120) {
            throw new BadRequestException("Idempotency-Key must have at most 120 characters");
        }
        return normalized;
    }

    private Map<String, Object> buildAuditPayload(UUID playerId, BossDefinitionEntity boss, WorldBossInstance instance, WorldBossAttack attack, List<WorldBossRewardResponse> rewards) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "world-boss");
        payload.put("operation", "attack");
        payload.put("playerId", playerId.toString());
        payload.put("bossCode", boss.getCode());
        payload.put("worldBossId", instance.getId().toString());
        payload.put("damage", attack.getDamage());
        payload.put("remainingHpAfter", attack.getRemainingHpAfter());
        payload.put("defeated", attack.isDefeated());
        payload.put("rewards", rewards.stream().map(reward -> Map.of("type", reward.rewardType(), "chestCode", reward.chestCode(), "chestName", reward.chestName())).toList());
        return payload;
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

    private int applyCostReduction(int baseCost, double multiplier) {
        if (multiplier >= 1.0) return baseCost;
        return Math.max(1, (int) Math.floor(baseCost * multiplier));
    }

    public AttackWorldBossUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final BossDefinitionRepository bossDefinitionRepository, final WorldBossInstanceRepository worldBossInstanceRepository, final WorldBossAttackRepository worldBossAttackRepository, final WorldBossService worldBossService, final WorldBossRewardService worldBossRewardService, final DigimonPowerService digimonPowerService, final ClanBonusService clanBonusService, final GlobalDamageBuffService globalDamageBuffService, final TransactionAuditPublisher transactionAuditPublisher, final GameplayConfig gameplayConfig, final ActivityCalendarService activityCalendarService) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.bossDefinitionRepository = bossDefinitionRepository;
        this.worldBossInstanceRepository = worldBossInstanceRepository;
        this.worldBossAttackRepository = worldBossAttackRepository;
        this.worldBossService = worldBossService;
        this.worldBossRewardService = worldBossRewardService;
        this.digimonPowerService = digimonPowerService;
        this.clanBonusService = clanBonusService;
        this.globalDamageBuffService = globalDamageBuffService;
        this.transactionAuditPublisher = transactionAuditPublisher;
        this.gameplayConfig = gameplayConfig;
        this.activityCalendarService = activityCalendarService;
    }
}
