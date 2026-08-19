package com.dro.modules.ranking.api.dto.response;

import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.Stage;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Ranking.
 */
public record RankingEntryResponse(
        int position,
        String digimonName,
        Stage digimonStage,
        int level,
        DigimonGrade grade,
        int rebirthCount,
        String playerName,
        UUID digimonId,
        UUID playerId
) {}
