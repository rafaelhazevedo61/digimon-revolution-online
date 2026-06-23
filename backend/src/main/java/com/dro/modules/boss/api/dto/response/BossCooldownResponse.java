package com.dro.modules.boss.api.dto.response;

public record BossCooldownResponse(
        String bossCode,
        String bossName,
        String bossType,
        boolean available,
        Long cooldownRemainingSeconds
) {}
