package com.dro.modules.boss.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
        String chestCode,
        @Min(0) @Max(100) Integer equipmentChance
) {}
