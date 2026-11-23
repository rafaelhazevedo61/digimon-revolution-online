package com.dro.modules.ranking.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TrainerRankingDtos {
    private TrainerRankingDtos() {}
    public record Entry(UUID playerId, String playerName, long position, BigDecimal totalScore,
                        BigDecimal collectionScore, BigDecimal achievementScore, BigDecimal towerScore,
                        long collectionEntries, long masteredSpecies, long completedAchievements, long highestFloor,
                        BigDecimal weeklyGain, Long weeklyPosition, Instant weeklyBaselineAt, String weeklyBaselineKind,
                        String movement, Long positionChange) {
        public Entry(UUID playerId, String playerName, long position, BigDecimal totalScore,
                     BigDecimal collectionScore, BigDecimal achievementScore, BigDecimal towerScore,
                     long collectionEntries, long masteredSpecies, long completedAchievements, long highestFloor) {
            this(playerId, playerName, position, totalScore, collectionScore, achievementScore, towerScore,
                    collectionEntries, masteredSpecies, completedAchievements, highestFloor,
                    BigDecimal.ZERO, null, null, null, "UNAVAILABLE", null);
        }
    }
    public record Profile(Entry ranking, UUID clanId, String clanName, String clanTag, Instant updatedAt) {}
    public record State(Instant updatedAt, long totalPlayers, LocalDate weekStart, Instant weekBaselineAt,
                        LocalDate comparisonDate, boolean comparisonAvailable, String rulesVersion) {
        public State(Instant updatedAt, long totalPlayers) { this(updatedAt, totalPlayers, null, null, null, false, null); }
    }
    public record Page(List<Entry> entries, Entry me, String myStatus, Instant updatedAt,
                       long totalPlayers, long matchingPlayers, int page, int size, boolean hasNext,
                       String mode, LocalDate weekStart, Instant weekBaselineAt,
                       LocalDate comparisonDate, boolean comparisonAvailable) {}
}
