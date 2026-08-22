package com.dro.modules.arena.api.dto.response;

/**
 * Contrato de dados do módulo de Arena.
 */
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
        int arenaCoinsBalance,
        String tier,
        String rewardChestCode,
        String rewardChestName
) {}
