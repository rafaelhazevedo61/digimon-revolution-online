package com.dro.modules.boss.world.api.dto.response;

import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;

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
        int attackCooldownMinutes,
        boolean cooldownEnabled,
        Instant nextAttackAvailableAt,
        long myTotalDamage,
        List<WorldBossRankingEntryResponse> ranking,
        List<WorldBossAttackResponse> recentAttacks,
        List<WorldBossRewardResponse> myRewards,
        BossDefeatSummaryResponse defeatSummary
) {
}
