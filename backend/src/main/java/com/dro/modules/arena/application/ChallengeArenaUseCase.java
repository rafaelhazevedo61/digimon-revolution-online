package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaMatchResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ChallengeArenaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ArenaMatchRepository arenaMatchRepository;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final GlobalDamageBuffService globalDamageBuffService;

    @Transactional
    public ArenaMatchResponse execute(String token, UUID opponentDigimonId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("You have no active Digimon to fight in the arena");
        }

        Digimon attacker = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        Digimon defender = digimonRepository.findById(opponentDigimonId)
                .orElseThrow(() -> new NotFoundException("Opponent not found"));

        if (defender.getPlayerId().equals(playerId)) {
            throw new BadRequestException("You cannot challenge your own Digimon");
        }

        if (defender.getStatus() != DigimonStatus.ACTIVE) {
            throw new BadRequestException("Opponent is not available");
        }

        boolean isAdmin = player.getUserType() == UserType.ADMIN;

        // Bots ignoram a janela de rating (só respeitam stage) para garantir oponentes
        // de preenchimento mesmo quando o jogador está com rating muito alto/baixo.
        if (!isAdmin && !defender.isBot()
                && !ArenaRules.withinChallengeWindow(attacker.getArenaRating(), defender.getArenaRating())) {
            throw new BadRequestException("Opponent out of your rating range (max "
                    + ArenaRules.RATING_WINDOW + " points difference)");
        }

        if (!isAdmin && !ArenaRules.withinStageRange(attacker.getStage(), defender.getStage())) {
            throw new BadRequestException("Opponent stage too far from yours");
        }

        if (!isAdmin) {
            Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            Instant dailyResetCutoff = player.getArenaDailyResetAt() != null
                    ? player.getArenaDailyResetAt().atZone(ZoneId.systemDefault()).toInstant()
                    : null;
            Instant attackCutoff = dailyResetCutoff != null && dailyResetCutoff.isAfter(startOfDay)
                    ? dailyResetCutoff
                    : startOfDay;
            long usedToday = arenaMatchRepository
                    .countByAttackerPlayerIdAndCreatedAtGreaterThanEqual(playerId, attackCutoff);
            if (ArenaRules.dailyLimitReached(usedToday)) {
                throw new BadRequestException("Daily challenge limit reached ("
                        + ArenaRules.DAILY_CHALLENGE_LIMIT + " per day). Come back tomorrow.");
            }

            arenaMatchRepository
                    .findFirstByAttackerPlayerIdAndDefenderDigimonIdOrderByCreatedAtDesc(playerId, opponentDigimonId)
                    .ifPresent(last -> {
                        Instant readyAt = last.getCreatedAt()
                                .plus(ArenaRules.TARGET_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
                        long secondsLeft = readyAt.getEpochSecond() - Instant.now().getEpochSecond();
                        if (secondsLeft > 0) {
                            throw new BadRequestException("You recently challenged this opponent. Try again in "
                                    + ((secondsLeft + 59) / 60) + " min.");
                        }
                    });
        }

        UUID attackerClanId = player.getClanId();

        if (!isAdmin) {
            int maxEnergyBonus = attackerClanId != null ? clanBonusService.getMaxEnergyBonus(attackerClanId) : 0;
            attacker.regenerateEnergy(maxEnergyBonus);
            int energyCost = attackerClanId != null
                    ? applyCostReduction(ArenaRules.ENERGY_COST, clanBonusService.getEnergyCostMultiplier(attackerClanId))
                    : ArenaRules.ENERGY_COST;
            if (attacker.getEnergy() < energyCost) {
                throw new BadRequestException("Not enough energy. Required: " + energyCost
                        + ", current: " + attacker.getEnergy());
            }
            attacker.consumeEnergy(energyCost);
        }

        double attackerPower = digimonPowerService.calculatePower(attacker) * globalDamageBuffService.getMultiplier();
        double defenderPower = digimonPowerService.calculatePower(defender);

        boolean buffActive = globalDamageBuffService.isEnabled();
        int winChance = buffActive ? 100 : ArenaRules.winChance(attackerPower, defenderPower);
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        boolean victory = buffActive || roll <= winChance;

        int attackerRatingBefore = attacker.getArenaRating();
        int defenderRatingBefore = defender.getArenaRating();

        double attackerExpected = ArenaRules.expectedScore(attackerRatingBefore, defenderRatingBefore);

        int attackerRatingAfter = ArenaRules.newRating(attackerRatingBefore, attackerExpected, victory ? 1 : 0);
        attacker.setArenaRating(attackerRatingAfter);

        // Bots são referências fixas: não têm rating/estatísticas alteradas nem persistidas.
        boolean defenderIsBot = defender.isBot();
        int defenderRatingAfter = defenderRatingBefore;
        if (!defenderIsBot) {
            double defenderExpected = ArenaRules.expectedScore(defenderRatingBefore, attackerRatingBefore);
            defenderRatingAfter = ArenaRules.newRating(defenderRatingBefore, defenderExpected, victory ? 0 : 1);
            defender.setArenaRating(defenderRatingAfter);
        }

        int bitsGained = 0;
        int arenaCoinsGained;
        if (victory) {
            if (attackerClanId != null) {
                clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.ARENA_WINS);
            }
            attacker.setArenaWins(attacker.getArenaWins() + 1);
            bitsGained = ArenaRules.winBits(attackerRatingBefore, defenderRatingBefore);
            attacker.setBits(attacker.getBits() + bitsGained);
            arenaCoinsGained = ArenaRules.winArenaCoins(winChance);
            if (!defenderIsBot) defender.setArenaLosses(defender.getArenaLosses() + 1);
        } else {
            attacker.setArenaLosses(attacker.getArenaLosses() + 1);
            arenaCoinsGained = ArenaRules.lossArenaCoins();
            if (!defenderIsBot) defender.setArenaWins(defender.getArenaWins() + 1);
        }

        if (attackerClanId != null) {
            clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.ARENA_DUELS);
        }

        player.setArenaCoins(player.getArenaCoins() + arenaCoinsGained);

        try {
            digimonRepository.save(attacker);
            if (!defenderIsBot) {
                digimonRepository.save(defender);
            }
            // Força o UPDATE (com checagem de @Version) antes de gravar a partida,
            // detectando desafios simultâneos ao mesmo defensor.
            digimonRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException(
                    "O oponente foi atualizado por outra partida ao mesmo tempo. Tente novamente.");
        }

        playerRepository.save(player);

        int attackerRatingChange = attackerRatingAfter - attackerRatingBefore;
        int defenderRatingChange = defenderRatingAfter - defenderRatingBefore;

        ArenaMatch match = ArenaMatch.builder()
                .id(UUID.randomUUID())
                .attackerPlayerId(playerId)
                .attackerDigimonId(attacker.getId())
                .defenderPlayerId(defender.getPlayerId())
                .defenderDigimonId(defender.getId())
                .attackerWon(victory)
                .attackerPower((int) Math.round(attackerPower))
                .defenderPower((int) Math.round(defenderPower))
                .winChance(winChance)
                .attackerRatingChange(attackerRatingChange)
                .attackerRatingAfter(attackerRatingAfter)
                .defenderRatingChange(defenderRatingChange)
                .defenderRatingAfter(defenderRatingAfter)
                .bitsGained(bitsGained)
                .createdAt(Instant.now())
                .build();

        arenaMatchRepository.save(match);

        return new ArenaMatchResponse(
                victory,
                defender.getName(),
                winChance,
                attackerPower,
                defenderPower,
                attackerRatingChange,
                attackerRatingAfter,
                bitsGained,
                arenaCoinsGained,
                player.getArenaCoins(),
                ArenaRules.tierFor(attackerRatingAfter).getLabel()
        );
    }

    private int applyCostReduction(int baseCost, double multiplier) {
        if (multiplier >= 1.0) return baseCost;
        return Math.max(1, (int) Math.floor(baseCost * multiplier));
    }
}
