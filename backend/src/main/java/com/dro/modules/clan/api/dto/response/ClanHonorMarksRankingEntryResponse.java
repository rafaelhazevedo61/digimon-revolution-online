package com.dro.modules.clan.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanHonorMarksRankingEntryResponse(
        UUID playerId,
        String username,
        long contribution
) {
}
