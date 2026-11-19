package com.dro.modules.clan.raid.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanRaidRankingEntryResponse(
        int position,
        UUID playerId,
        String username,
        long totalDamage
) {
}
