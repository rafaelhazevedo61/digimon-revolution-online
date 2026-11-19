package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaSeasonRankingEntryResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetArenaSeasonRankingUseCase {
    public static final LocalDate SEASON_START = LocalDate.of(2026, 8, 1);
    public static final LocalDate SEASON_END = LocalDate.of(2026, 12, 31);
    private final ArenaMatchRepository arenaMatchRepository;
    private final PlayerRepository playerRepository;

    public GetArenaSeasonRankingUseCase(ArenaMatchRepository arenaMatchRepository, PlayerRepository playerRepository) {
        this.arenaMatchRepository = arenaMatchRepository;
        this.playerRepository = playerRepository;
    }

    public List<ArenaSeasonRankingEntryResponse> execute(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);
        ZoneId zone = ZoneId.systemDefault();
        Instant start = SEASON_START.atStartOfDay(zone).toInstant();
        Instant endExclusive = SEASON_END.plusDays(1).atStartOfDay(zone).toInstant();
        Map<UUID, long[]> totals = new HashMap<>();

        for (ArenaMatch match : arenaMatchRepository.findByCreatedAtBetween(start, endExclusive)) {
            addResult(totals, match.getAttackerPlayerId(), match.isAttackerWon(), match.getAttackerRatingChange());
            addResult(totals, match.getDefenderPlayerId(), !match.isAttackerWon(), match.getDefenderRatingChange());
        }

        List<UUID> orderedIds = totals.entrySet().stream()
                .sorted(Comparator.<Map.Entry<UUID, long[]>>comparingLong(e -> e.getValue()[2]).reversed()
                        .thenComparing(Comparator.comparingLong((Map.Entry<UUID, long[]> e) -> e.getValue()[0]).reversed()))
                .map(Map.Entry::getKey)
                .toList();
        int from = Math.min(safePage * safeSize, orderedIds.size());
        int to = Math.min(from + safeSize, orderedIds.size());
        Map<UUID, String> names = playerRepository.findAllById(orderedIds.subList(from, to)).stream()
                .collect(java.util.stream.Collectors.toMap(Player::getId, Player::getUsername));

        List<ArenaSeasonRankingEntryResponse> result = new ArrayList<>();
        for (int i = from; i < to; i++) {
            UUID playerId = orderedIds.get(i);
            long[] values = totals.get(playerId);
            result.add(new ArenaSeasonRankingEntryResponse(
                    i + 1, playerId, names.getOrDefault(playerId, "Unknown"),
                    values[0], values[1], values[2], values[3], values[4]
            ));
        }
        return result;
    }

    private void addResult(Map<UUID, long[]> totals, UUID playerId, boolean won, int ratingChange) {
        if (playerId == null) return;
        long[] values = totals.computeIfAbsent(playerId, ignored -> new long[5]);
        long points = Math.abs((long) ratingChange);
        if (won) {
            values[0] += points;
            values[3]++;
        } else {
            values[1] += points;
            values[4]++;
        }
        values[2] = values[0] - values[1];
    }
}
