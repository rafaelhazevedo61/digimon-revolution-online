package com.dro.modules.ranking.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TrainerRankingDtos {
    private TrainerRankingDtos() {}
    public record Entry(UUID playerId, String playerName, long position, BigDecimal totalScore,
                        BigDecimal collectionScore, BigDecimal achievementScore, BigDecimal towerScore,
                        long collectionEntries, long masteredSpecies, long completedAchievements, long highestFloor) {}
    public record Profile(Entry ranking, UUID clanId, String clanName, String clanTag, Instant updatedAt) {}
    public record State(Instant updatedAt, long totalPlayers) {}
    public record Page(List<Entry> entries, Entry me, String myStatus, Instant updatedAt,
                       long totalPlayers, long matchingPlayers, int page, int size, boolean hasNext) {}
}
