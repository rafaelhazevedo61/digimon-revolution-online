package com.dro.modules.clan.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanRankingEntryResponse(
        int position,
        UUID id,
        String name,
        String tag,
        int memberCount,
        long totalPower
) {
}
