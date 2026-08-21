package com.dro.modules.boss.api.dto.request;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record UpdateBossRequest(
        String name,
        String bossType,
        String requiredStage,
        Integer requiredLevel,
        Integer requiredRebirths,
        Integer hp,
        Integer atk,
        Integer def,
        Integer energyCost,
        Integer cooldownMinutes,
        Integer baseXpReward,
        Integer baseBitsReward,
        Integer defeatXpPercent,
        String imageUrl,
        Boolean active,
        String chestCode
) {}
