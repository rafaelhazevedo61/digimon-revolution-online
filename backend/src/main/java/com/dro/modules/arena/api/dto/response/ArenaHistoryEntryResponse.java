package com.dro.modules.arena.api.dto.response;

import java.time.Instant;

/**
 * Contrato de dados do módulo de Arena.
 */
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
