package com.dro.modules.arena.api.dto.response;

public record ArenaMatchResponse(
        boolean victory,
        String opponentName,
        int winChance,
        double attackerPower,
        double defenderPower,
        int ratingChange,
        int newRating,
        int bitsGained,
        int arenaCoinsGained,
        int arenaCoinsBalance
) {}
