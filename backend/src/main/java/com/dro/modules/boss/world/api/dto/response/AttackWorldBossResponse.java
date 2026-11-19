package com.dro.modules.boss.world.api.dto.response;

import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record AttackWorldBossResponse(
        UUID worldBossId,
        String bossCode,
        String bossName,
        int damage,
        int remainingHp,
        int maxHp,
        boolean defeated,
        int winChance,
        int xpGained,
        int bitsGained,
        int defeatedRewardXp,
        int defeatedRewardBits,
        List<WorldBossRewardResponse> rewards,
        BossDefeatSummaryResponse defeatSummary
) {
}
