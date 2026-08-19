package com.dro.modules.boss.world.api.dto.response;

import com.dro.modules.boss.world.domain.WorldBossStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record WorldBossResponse(
        UUID id,
        String bossCode,
        String bossName,
        String bossImageUrl,
        int maxHp,
        int remainingHp,
        WorldBossStatus status,
        Instant createdAt,
        Instant defeatedAt,
        int myDailyAttacksUsed,
        int myDailyAttacksRemaining,
        long myTotalDamage,
        List<WorldBossRankingEntryResponse> ranking,
        List<WorldBossAttackResponse> recentAttacks
) {
}
