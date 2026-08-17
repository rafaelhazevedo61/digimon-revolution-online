package com.dro.modules.boss.world.api.dto.response;

import java.util.UUID;

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
        int dailyAttacksRemaining
) {
}
