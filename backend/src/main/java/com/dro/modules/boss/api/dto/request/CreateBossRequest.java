package com.dro.modules.boss.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record CreateBossRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String bossType,
        @NotBlank String requiredStage,
        @NotNull Integer requiredLevel,
        @NotNull Integer requiredRebirths,
        @NotNull Integer hp,
        @NotNull Integer atk,
        @NotNull Integer def,
        @NotNull Integer energyCost,
        @NotNull Integer cooldownMinutes,
        Boolean cooldownEnabled,
        @NotNull Integer baseXpReward,
        @NotNull Integer baseBitsReward,
        Integer defeatXpPercent,
        String imageUrl,
        String chestCode
) {}
