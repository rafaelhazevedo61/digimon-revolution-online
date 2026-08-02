package com.dro.modules.arena.api.dto.response;

import com.dro.modules.digimon.domain.enums.Stage;

import java.util.UUID;

public record ArenaRankingEntryResponse(
        int position,
        String digimonName,
        String playerName,
        Stage stage,
        int level,
        int rating,
        int wins,
        int losses,
        UUID digimonId,
        UUID playerId
) {}
