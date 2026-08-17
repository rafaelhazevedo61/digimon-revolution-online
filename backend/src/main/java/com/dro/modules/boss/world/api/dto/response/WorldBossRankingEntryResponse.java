package com.dro.modules.boss.world.api.dto.response;

import java.util.UUID;

public record WorldBossRankingEntryResponse(
        int position,
        UUID playerId,
        String username,
        long totalDamage
) {
}
