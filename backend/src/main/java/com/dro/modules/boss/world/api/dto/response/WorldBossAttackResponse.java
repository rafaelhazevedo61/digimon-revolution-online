package com.dro.modules.boss.world.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WorldBossAttackResponse(
        UUID id,
        UUID playerId,
        String username,
        int damage,
        Instant createdAt
) {
}
