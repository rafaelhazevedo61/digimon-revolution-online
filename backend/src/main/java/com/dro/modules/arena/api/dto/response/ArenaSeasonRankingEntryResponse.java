package com.dro.modules.arena.api.dto.response;

import java.util.UUID;

public record ArenaSeasonRankingEntryResponse(
        int position,
        UUID playerId,
        String playerName,
        long pointsWon,
        long pointsLost,
        long netPoints,
        long wins,
        long losses
) {
}
