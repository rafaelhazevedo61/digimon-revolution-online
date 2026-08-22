package com.dro.modules.boss.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record BossDefinitionResponse(
        Long id,
        String code,
        String name,
        String bossType,
        String requiredStage,
        int requiredLevel,
        int requiredRebirths,
        int hp,
        int atk,
        int def,
        int energyCost,
        int cooldownMinutes,
        boolean cooldownEnabled,
        int baseXpReward,
        int baseBitsReward,
        String imageUrl,
        boolean available,
        Long cooldownRemainingSeconds,
        Integer winChance,
        String chestCode,
        String chestName,
        List<BossDropResponse> drops
) {}
