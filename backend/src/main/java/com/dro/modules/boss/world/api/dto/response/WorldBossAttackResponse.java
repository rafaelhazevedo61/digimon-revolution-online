package com.dro.modules.boss.world.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record WorldBossAttackResponse(
        UUID id,
        UUID playerId,
        String username,
        int damage,
        Instant createdAt
) {
}
