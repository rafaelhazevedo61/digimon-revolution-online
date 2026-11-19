package com.dro.modules.boss.world.api.dto.response;

/**
 * Baú concedido ao jogador durante o ciclo do Boss Mundial.
 */
public record WorldBossRewardResponse(
        String rewardType,
        String chestCode,
        String chestName
) {
}
