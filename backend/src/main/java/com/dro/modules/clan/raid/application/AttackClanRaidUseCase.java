package com.dro.modules.clan.raid.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.raid.api.dto.response.AttackClanRaidResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidRules;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
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
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AttackClanRaidUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ClanRepository clanRepository;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final ClanRaidRepository clanRaidRepository;
    private final ClanRaidAttackRepository clanRaidAttackRepository;
    private final ClanRaidService clanRaidService;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;

    @Transactional
    public AttackClanRaidResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() == null) {
            throw new BadRequestException("You must be in a clan to attack the raid");
        }

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active Digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }

        ClanRaid raid = clanRaidService.getOrCreateToday(player.getClanId());

        if (raid.getStatus() == ClanRaidStatus.DEFEATED || raid.getRemainingHp() <= 0) {
            throw new BadRequestException("The clan raid has already been defeated today");
        }

        BossDefinitionEntity boss = bossDefinitionRepository.findById(raid.getBossId())
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        validateRequirements(boss, digimon);

        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant resetCutoff = raid.getDailyResetAt() != null && raid.getDailyResetAt().isAfter(startOfDay)
                ? raid.getDailyResetAt()
                : startOfDay;

        long usedToday = clanRaidAttackRepository
                .countByClanRaidIdAndPlayerIdAndCreatedAtGreaterThanEqual(raid.getId(), playerId, resetCutoff);
        if (ClanRaidRules.dailyLimitReached(usedToday)) {
            throw new BadRequestException("Daily raid attack limit reached (" + ClanRaidRules.DAILY_ATTACK_LIMIT + " per day). Come back tomorrow.");
        }

        int maxEnergyBonus = clanBonusService.getMaxEnergyBonus(player.getClanId());
        digimon.regenerateEnergy(maxEnergyBonus);
        int baseEnergyCost = boss.getEnergyCost();
        int energyCost = applyCostReduction(baseEnergyCost, clanBonusService.getEnergyCostMultiplier(player.getClanId()));
        if (digimon.getEnergy() < energyCost) {
            throw new BadRequestException("Not enough energy. Required: " + energyCost + ", current: " + digimon.getEnergy());
        }
        digimon.consumeEnergy(energyCost);

        double digimonPower = digimonPowerService.calculatePower(digimon, player.getClanId());
        double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());
        int winChance = BossCombatRules.calculateWinChance(digimonPower, bossPower);

        int damage = ClanRaidRules.calculateDamage(raid.getMaxHp(), winChance);
        int actualDamage = Math.min(damage, raid.getRemainingHp());

        int xpGained = ClanRaidRules.hitXp(boss.getBaseXpReward(), boss.getDefeatXpPercent());
        int bitsGained = ClanRaidRules.hitBits(boss.getBaseBitsReward(), boss.getDefeatXpPercent());

        digimon.gainExperience(xpGained);
        digimon.setBits(digimon.getBits() + bitsGained);

        raid.setRemainingHp(raid.getRemainingHp() - actualDamage);
        raid.setUpdatedAt(Instant.now());

        boolean defeated = false;
        int clanHonorMarksGained = 0;
        int clanXpGained = 0;

        if (raid.getRemainingHp() <= 0) {
            defeated = true;
            raid.setStatus(ClanRaidStatus.DEFEATED);
            raid.setDefeatedAt(Instant.now());

            Clan clan = clanRepository.findById(player.getClanId())
                    .orElseThrow(() -> new NotFoundException("Clan not found"));

            double honorMarksMultiplier = 1.0 + clanBonusService.getHonorMarksBonusPercent(clan.getId());
            clanHonorMarksGained = (int) Math.floor(boss.getBaseBitsReward() * honorMarksMultiplier);
            clanXpGained = boss.getBaseXpReward();

            clan.setHonorMarks(clan.getHonorMarks() + clanHonorMarksGained);
            ClanRules.addExperience(clan, clanXpGained);
            clanRepository.save(clan);
        }

        ClanRaidAttack attack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raid.getId())
                .playerId(playerId)
                .digimonId(digimon.getId())
                .damage(actualDamage)
                .energyCost(energyCost)
                .bitsGained(bitsGained)
                .xpGained(xpGained)
                .createdAt(Instant.now())
                .build();

        digimonRepository.save(digimon);
        clanRaidRepository.save(raid);
        clanRaidAttackRepository.save(attack);

        long remainingAttacks = ClanRaidRules.dailyAttacksRemaining(usedToday + 1);

        return new AttackClanRaidResponse(
                raid.getId(),
                boss.getCode(),
                boss.getName(),
                actualDamage,
                raid.getRemainingHp(),
                raid.getMaxHp(),
                defeated,
                winChance,
                xpGained,
                bitsGained,
                clanHonorMarksGained,
                clanXpGained,
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
