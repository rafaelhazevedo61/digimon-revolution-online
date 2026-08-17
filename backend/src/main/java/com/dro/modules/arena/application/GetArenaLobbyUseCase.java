package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaLobbyResponse;
import com.dro.modules.arena.api.dto.response.ArenaOpponentResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetArenaLobbyUseCase {

    private static final int MAX_OPPONENTS = 15;
    private static final int MIN_LIST = 10;

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonPowerService digimonPowerService;
    private final ArenaMatchRepository arenaMatchRepository;
    private final ClanBonusService clanBonusService;

    public ArenaLobbyResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("You have no active Digimon to fight in the arena");
        }

        Digimon me = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        UUID myClanId = player.getClanId();
        int maxEnergyBonus = myClanId != null ? clanBonusService.getMaxEnergyBonus(myClanId) : 0;
        me.regenerateEnergy(maxEnergyBonus);
        double myPower = digimonPowerService.calculatePower(me, myClanId);

        List<Digimon> candidates = digimonRepository
                .findByStatusAndPlayerIdNot(DigimonStatus.ACTIVE, playerId);

        Comparator<Digimon> byProximity =
                Comparator.comparingInt(d -> Math.abs(d.getArenaRating() - me.getArenaRating()));

        // Jogadores reais respeitam janela de rating (±200) + stage.
        List<Digimon> reals = candidates.stream()
                .filter(d -> !d.isBot())
                .filter(d -> ArenaRules.withinChallengeWindow(me.getArenaRating(), d.getArenaRating()))
                .filter(d -> ArenaRules.withinStageRange(me.getStage(), d.getStage()))
                .sorted(byProximity)
                .limit(MAX_OPPONENTS)
                .toList();

        // Bots preenchem até MIN_LIST ignorando a janela de rating (só respeitam stage),
        // garantindo sempre >= 10 oponentes mesmo quando o jogador sobe muito de rating.
        List<Digimon> nearest = new ArrayList<>(reals);
        if (nearest.size() < MIN_LIST) {
            candidates.stream()
                    .filter(Digimon::isBot)
                    .filter(d -> ArenaRules.withinStageRange(me.getStage(), d.getStage()))
                    .sorted(byProximity)
                    .limit((long) MIN_LIST - nearest.size())
                    .forEach(nearest::add);
        }

        List<UUID> opponentPlayerIds = nearest.stream()
                .map(Digimon::getPlayerId)
                .distinct()
                .toList();

        Map<UUID, String> playerNames = playerRepository.findAllById(opponentPlayerIds)
                .stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));

        Instant now = Instant.now();
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

        Instant cooldownSince = now.minus(ArenaRules.TARGET_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
        Map<UUID, Instant> lastChallengePerTarget = arenaMatchRepository
                .findByAttackerPlayerIdAndCreatedAtGreaterThanEqual(playerId, cooldownSince)
                .stream()
                .collect(Collectors.toMap(
                        ArenaMatch::getDefenderDigimonId,
                        ArenaMatch::getCreatedAt,
                        (a, b) -> a.isAfter(b) ? a : b));

        List<ArenaOpponentResponse> opponents = nearest.stream().map(d -> {
            double power = digimonPowerService.calculatePower(d);
            int winChance = ArenaRules.winChance(myPower, power);
            int cooldownSecondsRemaining = 0;
            Instant lastChallenge = lastChallengePerTarget.get(d.getId());
            if (lastChallenge != null) {
                Instant readyAt = lastChallenge.plus(ArenaRules.TARGET_COOLDOWN_MINUTES, ChronoUnit.MINUTES);
                cooldownSecondsRemaining = (int) Math.max(0, readyAt.getEpochSecond() - now.getEpochSecond());
            }
            return new ArenaOpponentResponse(
                    d.getId(),
                    d.getName(),
                    playerNames.getOrDefault(d.getPlayerId(), "Unknown"),
                    d.getStage(),
                    d.getLevel(),
                    d.getArenaRating(),
                    (int) Math.round(power),
                    winChance,
                    ArenaRules.winBits(me.getArenaRating(), d.getArenaRating()),
                    d.isBot(),
                    cooldownSecondsRemaining,
                    ArenaRules.tierFor(d.getArenaRating()).getLabel()
            );
        }).toList();

        return new ArenaLobbyResponse(
                me.getName(),
                me.getArenaRating(),
                me.getArenaWins(),
                me.getArenaLosses(),
                (int) Math.round(myPower),
                me.getEnergy(),
                ArenaRules.ENERGY_COST,
                ArenaRules.DAILY_CHALLENGE_LIMIT,
                (int) usedToday,
                ArenaRules.remainingDailyChallenges(usedToday),
                player.getArenaCoins(),
                ArenaRules.tierFor(me.getArenaRating()).getLabel(),
                ArenaRules.tierFor(me.getArenaRating()).next() == null
                        ? null
                        : ArenaRules.tierFor(me.getArenaRating()).next().getLabel(),
                ArenaRules.pointsToNextTier(me.getArenaRating()),
                opponents
        );
    }
}
