package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaMatchResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.arena.domain.ArenaTier;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.activitycalendar.application.ActivityCalendarService;
import com.dro.modules.activitycalendar.domain.ActivitySource;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Arena.
 */
@Service
public class ChallengeArenaUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ArenaMatchRepository arenaMatchRepository;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final GlobalDamageBuffService globalDamageBuffService;
    private final AddItemUseCase addItemUseCase;
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final TransactionAuditPublisher transactionAuditPublisher;
    private final GameplayConfig gameplayConfig;
    private final ActivityCalendarService activityCalendarService;

    /**
     * Executa um desafio de Arena e persiste todos os efeitos da partida.
     *
     * <p>Em caso de vitória, o Baú é resolvido pelo tier do atacante após a
     * atualização do rating e creditado no inventário na mesma transação da
     * partida e da auditoria Outbox. Derrotas não concedem Baú.</p>
     *
     * @param token token JWT do jogador atacante
     * @param opponentDigimonId identificador do Digimon desafiado
     * @return resultado da partida, incluindo o Baú quando houver vitória
     */
    @Transactional
    public ArenaMatchResponse execute(String token, UUID opponentDigimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("You have no active Digimon to fight in the arena");
        }
        Digimon attacker = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active Digimon not found"));
        Digimon defender = digimonRepository.findById(opponentDigimonId).orElseThrow(() -> new NotFoundException("Opponent not found"));
        if (defender.getPlayerId().equals(playerId)) {
            throw new BadRequestException("You cannot challenge your own Digimon");
        }
        if (defender.getStatus() != DigimonStatus.ACTIVE) {
            throw new BadRequestException("Opponent is not available");
        }
        boolean isAdmin = player.getUserType() == UserType.ADMIN;
        // Bots ignoram a janela de rating (só respeitam stage) para garantir oponentes
        // de preenchimento mesmo quando o jogador está com rating muito alto/baixo.
        if (!isAdmin && !defender.isBot() && !ArenaRules.withinChallengeWindow(attacker.getArenaRating(), defender.getArenaRating())) {
            throw new BadRequestException("Opponent out of your rating range (max " + ArenaRules.RATING_WINDOW + " points difference)");
        }
        if (!isAdmin && !ArenaRules.withinStageRange(attacker.getStage(), defender.getStage())) {
            throw new BadRequestException("Opponent stage too far from yours");
        }
        if (!isAdmin) {
            Instant startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant dailyResetCutoff = player.getArenaDailyResetAt() != null ? player.getArenaDailyResetAt().atZone(ZoneId.systemDefault()).toInstant() : null;
            Instant attackCutoff = dailyResetCutoff != null && dailyResetCutoff.isAfter(startOfDay) ? dailyResetCutoff : startOfDay;
            long usedToday = arenaMatchRepository.countByAttackerPlayerIdAndCreatedAtGreaterThanEqual(playerId, attackCutoff);
            int dailyChallengeLimit = gameplayConfig.getArenaDailyChallengeLimit();
            if (ArenaRules.dailyLimitReached(usedToday, dailyChallengeLimit)) {
                throw new BadRequestException("Daily challenge limit reached (" + dailyChallengeLimit + " per day). Come back tomorrow.");
            }
            arenaMatchRepository.findFirstByAttackerPlayerIdAndDefenderDigimonIdOrderByCreatedAtDesc(playerId, opponentDigimonId).ifPresent(last -> {
                Instant readyAt = last.getCreatedAt().plus(ArenaRules.TARGET_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
                long secondsLeft = readyAt.getEpochSecond() - Instant.now().getEpochSecond();
                if (secondsLeft > 0) {
                    throw new BadRequestException("You recently challenged this opponent. Try again in " + ((secondsLeft + 59) / 60) + " min.");
                }
            });
        }
        UUID attackerClanId = player.getClanId();
        if (!isAdmin && gameplayConfig.isEnergyConsumptionEnabled()) {
            int maxEnergyBonus = attackerClanId != null ? clanBonusService.getMaxEnergyBonus(attackerClanId) : 0;
            attacker.regenerateEnergy(maxEnergyBonus);
            int energyCost = attackerClanId != null ? applyCostReduction(ArenaRules.ENERGY_COST, clanBonusService.getEnergyCostMultiplier(attackerClanId)) : ArenaRules.ENERGY_COST;
            if (attacker.getEnergy() < energyCost) {
                throw new BadRequestException("Not enough energy. Required: " + energyCost + ", current: " + attacker.getEnergy());
            }
            attacker.consumeEnergy(energyCost);
        }
        double attackerPower = digimonPowerService.calculatePower(attacker) * globalDamageBuffService.getMultiplier();
        double defenderPower = digimonPowerService.calculatePower(defender);
        boolean buffActive = globalDamageBuffService.isEnabled();
        int winChance = buffActive ? 100 : ArenaRules.winChance(attackerPower, defenderPower);
        int roll = ArenaRules.roll();
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
        ChestDefinitionEntity rewardChest = victory ? resolveRewardChest(ArenaRules.tierFor(attackerRatingAfter)) : null;
        if (victory) {
            if (attackerClanId != null) {
                clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.ARENA_WINS);
            }
            attacker.setArenaWins(attacker.getArenaWins() + 1);
            bitsGained = ArenaRules.winBits(attackerRatingBefore, defenderRatingBefore);
            attacker.setBits(attacker.getBits() + bitsGained);
            arenaCoinsGained = ArenaRules.winArenaCoins(winChance);
            addItemUseCase.addMaterial(attacker.getId(), rewardChest.getItemDefinition(), 1);
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
            throw new ConflictException("O oponente foi atualizado por outra partida ao mesmo tempo. Tente novamente.");
        }
        playerRepository.save(player);
        int attackerRatingChange = attackerRatingAfter - attackerRatingBefore;
        int defenderRatingChange = defenderRatingAfter - defenderRatingBefore;
        ArenaMatch match = ArenaMatch.builder().id(UUID.randomUUID()).attackerPlayerId(playerId).attackerDigimonId(attacker.getId()).defenderPlayerId(defender.getPlayerId()).defenderDigimonId(defender.getId()).attackerWon(victory).attackerPower((int) Math.round(attackerPower)).defenderPower((int) Math.round(defenderPower)).winChance(winChance).attackerRatingChange(attackerRatingChange).attackerRatingAfter(attackerRatingAfter).defenderRatingChange(defenderRatingChange).defenderRatingAfter(defenderRatingAfter).bitsGained(bitsGained).rewardChest(rewardChest).createdAt(Instant.now()).build();
        arenaMatchRepository.save(match);
        if (activityCalendarService != null) activityCalendarService.recordActivity(playerId, ActivitySource.ARENA_MATCH, match.getId().toString());
        transactionAuditPublisher.success("arena-challenge:" + match.getId(), "ARENA_CHALLENGED", "ArenaMatch", match.getId().toString(), buildAuditPayload(playerId, attacker, defender, match, rewardChest));
        return new ArenaMatchResponse(victory, defender.getName(), winChance, attackerPower, defenderPower, attackerRatingChange, attackerRatingAfter, bitsGained, arenaCoinsGained, player.getArenaCoins(), ArenaRules.tierFor(attackerRatingAfter).getLabel(), rewardChest != null ? rewardChest.getCode() : null, rewardChest != null ? rewardChest.getName() : null);
    }

    private ChestDefinitionEntity resolveRewardChest(ArenaTier tier) {
        String chestCode = "CHEST_ARENA_" + tier.name();
        ChestDefinitionEntity chest = chestDefinitionRepository.findWithCatalogByCode(chestCode).orElseThrow(() -> new ConflictException("Baú de recompensa da Arena não encontrado: " + chestCode));
        if (!chest.isActive()) {
            throw new ConflictException("Baú de recompensa da Arena está inativo: " + chestCode);
        }
        if (chest.getLootTable() == null || !chest.getLootTable().isActive()) {
            throw new ConflictException("Loot Table do Baú de recompensa da Arena está inativa: " + chestCode);
        }
        return chest;
    }

    private Map<String, Object> buildAuditPayload(UUID playerId, Digimon attacker, Digimon defender, ArenaMatch match, ChestDefinitionEntity rewardChest) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "arena");
        payload.put("operation", "challenge");
        payload.put("playerId", playerId.toString());
        payload.put("attackerDigimonId", attacker.getId().toString());
        payload.put("defenderDigimonId", defender.getId().toString());
        payload.put("victory", match.isAttackerWon());
        payload.put("attackerRatingAfter", match.getAttackerRatingAfter());
        payload.put("tier", ArenaRules.tierFor(match.getAttackerRatingAfter()).name());
        payload.put("bitsGained", match.getBitsGained());
        payload.put("rewardChestCode", rewardChest != null ? rewardChest.getCode() : null);
        payload.put("rewardChestName", rewardChest != null ? rewardChest.getName() : null);
        payload.put("summary", match.isAttackerWon() ? "Arena victory and reward chest granted" : "Arena defeat");
        return payload;
    }

    private int applyCostReduction(int baseCost, double multiplier) {
        if (multiplier >= 1.0) return baseCost;
        return Math.max(1, (int) Math.floor(baseCost * multiplier));
    }

    public ChallengeArenaUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final ArenaMatchRepository arenaMatchRepository, final DigimonPowerService digimonPowerService, final ClanBonusService clanBonusService, final ClanMissionProgressTracker clanMissionProgressTracker, final GlobalDamageBuffService globalDamageBuffService, final AddItemUseCase addItemUseCase, final ChestDefinitionRepository chestDefinitionRepository, final TransactionAuditPublisher transactionAuditPublisher, final GameplayConfig gameplayConfig, final ActivityCalendarService activityCalendarService) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.arenaMatchRepository = arenaMatchRepository;
        this.digimonPowerService = digimonPowerService;
        this.clanBonusService = clanBonusService;
        this.clanMissionProgressTracker = clanMissionProgressTracker;
        this.globalDamageBuffService = globalDamageBuffService;
        this.addItemUseCase = addItemUseCase;
        this.chestDefinitionRepository = chestDefinitionRepository;
        this.transactionAuditPublisher = transactionAuditPublisher;
        this.gameplayConfig = gameplayConfig;
        this.activityCalendarService = activityCalendarService;
    }
}
