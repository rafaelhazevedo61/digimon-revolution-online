package com.dro.modules.boss.world.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.api.dto.response.AttackWorldBossResponse;
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
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Boss Mundial.
 */
@Service
@RequiredArgsConstructor
public class AttackWorldBossUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final WorldBossAttackRepository worldBossAttackRepository;
    private final WorldBossService worldBossService;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;
    private final GlobalDamageBuffService globalDamageBuffService;

    @Transactional
    public AttackWorldBossResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active Digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }

        WorldBossInstance instance = worldBossService.getOrCreateToday();

        if (instance.getStatus() == WorldBossStatus.DEFEATED || instance.getRemainingHp() <= 0) {
            throw new BadRequestException("The world boss has already been defeated today");
        }

        BossDefinitionEntity boss = bossDefinitionRepository.findById(instance.getBossId())
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        validateRequirements(boss, digimon);

        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant resetCutoff = instance.getDailyResetAt() != null && instance.getDailyResetAt().isAfter(startOfDay)
                ? instance.getDailyResetAt()
                : startOfDay;

        long usedToday = worldBossAttackRepository
                .countByWorldBossIdAndPlayerIdAndCreatedAtGreaterThanEqual(instance.getId(), playerId, resetCutoff);
        if (WorldBossRules.dailyLimitReached(usedToday)) {
            throw new BadRequestException("Daily world boss attack limit reached (" + WorldBossRules.DAILY_ATTACK_LIMIT + " per day). Come back tomorrow.");
        }

        UUID clanId = player.getClanId();
        int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;
        digimon.regenerateEnergy(maxEnergyBonus);
        int baseEnergyCost = boss.getEnergyCost();
        int energyCost = applyCostReduction(baseEnergyCost, clanId != null ? clanBonusService.getEnergyCostMultiplier(clanId) : 1.0);
        if (digimon.getEnergy() < energyCost) {
            throw new BadRequestException("Not enough energy. Required: " + energyCost + ", current: " + digimon.getEnergy());
        }
        digimon.consumeEnergy(energyCost);

        double digimonPower = digimonPowerService.calculatePower(digimon, clanId);
        double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());
        int winChance = WorldBossRules.calculateWinChance(digimonPower, bossPower);

        int damage = (int) Math.round(WorldBossRules.calculateDamage(instance.getMaxHp(), winChance) * globalDamageBuffService.getMultiplier());
        int actualDamage = Math.min(damage, instance.getRemainingHp());

        int xpGained = WorldBossRules.hitXp(boss.getBaseXpReward(), boss.getDefeatXpPercent());
        int bitsGained = WorldBossRules.hitBits(boss.getBaseBitsReward(), boss.getDefeatXpPercent());

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

            defeatedRewardXp = boss.getBaseXpReward();
            defeatedRewardBits = boss.getBaseBitsReward();

            digimon.gainExperience(defeatedRewardXp);
            digimon.setBits(digimon.getBits() + defeatedRewardBits);
        }

        WorldBossAttack attack = WorldBossAttack.builder()
                .id(UUID.randomUUID())
                .worldBossId(instance.getId())
                .playerId(playerId)
                .digimonId(digimon.getId())
                .damage(actualDamage)
                .energyCost(energyCost)
                .bitsGained(bitsGained + defeatedRewardBits)
                .xpGained(xpGained + defeatedRewardXp)
                .createdAt(Instant.now())
                .build();

        digimonRepository.save(digimon);
        worldBossInstanceRepository.save(instance);
        worldBossAttackRepository.save(attack);

        long remainingAttacks = WorldBossRules.dailyAttacksRemaining(usedToday + 1);

        return new AttackWorldBossResponse(
                instance.getId(),
                boss.getCode(),
                boss.getName(),
                actualDamage,
                instance.getRemainingHp(),
                instance.getMaxHp(),
                defeated,
                winChance,
                xpGained,
                bitsGained,
                defeatedRewardXp,
                defeatedRewardBits,
                (int) remainingAttacks
        );
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
}
