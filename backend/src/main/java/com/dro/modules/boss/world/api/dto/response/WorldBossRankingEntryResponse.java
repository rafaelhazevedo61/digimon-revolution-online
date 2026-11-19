package com.dro.modules.boss.world.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record WorldBossRankingEntryResponse(
        int position,
        UUID playerId,
        String username,
        long totalDamage
) {
}
