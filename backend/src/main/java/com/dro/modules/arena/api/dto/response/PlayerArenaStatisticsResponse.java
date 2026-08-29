package com.dro.modules.arena.api.dto.response;

import com.dro.modules.arena.domain.PlayerArenaStatistics;

public record PlayerArenaStatisticsResponse(
        long pointsWon,
        long pointsLost,
        long netPoints,
        long wins,
        long losses
) {
    public static PlayerArenaStatisticsResponse from(PlayerArenaStatistics statistics) {
        return new PlayerArenaStatisticsResponse(
                statistics.getArenaPointsWon(),
                statistics.getArenaPointsLost(),
                statistics.getNetPoints(),
                statistics.getArenaWins(),
                statistics.getArenaLosses()
        );
    }
}
