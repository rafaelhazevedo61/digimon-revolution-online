package com.dro.modules.arena.api.dto.response;

import java.time.Instant;

public record ArenaHistoryEntryResponse(
        boolean attacker,
        boolean won,
        String opponentName,
        int myPower,
        int opponentPower,
        int ratingChange,
        int bitsGained,
        Instant createdAt
) {}
