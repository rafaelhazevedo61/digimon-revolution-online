package com.dro.modules.boss.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Resumo estatístico apresentado quando um chefe é derrotado. */
public record BossDefeatSummaryResponse(
        UUID finalBlowPlayerId,
        String finalBlowUsername,
        UUID topDamagePlayerId,
        String topDamageUsername,
        long topDamage,
        int totalAttacks,
        long aliveDurationSeconds,
        Instant nextCycleAt
) {
}
