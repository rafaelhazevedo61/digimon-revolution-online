package com.dro.modules.boss.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BossAttemptResponse(
        UUID id,
        String bossCode,
        String bossName,
        String status,
        int xpGained,
        int bitsGained,
        Instant createdAt
) {}
