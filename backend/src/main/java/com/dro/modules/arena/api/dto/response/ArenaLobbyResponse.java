package com.dro.modules.arena.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Arena.
 */
public record ArenaLobbyResponse(
        String digimonName,
        int rating,
        int wins,
        int losses,
        int power,
        int energy,
        int energyCost,
        int dailyChallengeLimit,
        int challengesUsedToday,
        int challengesRemaining,
        int arenaCoins,
        String tier,
        String nextTier,
        int pointsToNextTier,
        List<ArenaOpponentResponse> opponents
) {}
