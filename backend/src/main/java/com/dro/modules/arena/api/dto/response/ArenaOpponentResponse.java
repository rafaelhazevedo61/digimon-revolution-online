package com.dro.modules.arena.api.dto.response;

import com.dro.modules.digimon.domain.enums.Stage;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Arena.
 */
public record ArenaOpponentResponse(
        UUID digimonId,
        String digimonName,
        String playerName,
        Stage stage,
        int level,
        int rating,
        int power,
        int winChance,
        int bitsReward,
        boolean bot,
        int cooldownSecondsRemaining,
        String tier
) {}
