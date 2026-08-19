package com.dro.modules.boss.api.dto.response;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record BossCooldownResponse(
        String bossCode,
        String bossName,
        String bossType,
        boolean available,
        Long cooldownRemainingSeconds
) {}
