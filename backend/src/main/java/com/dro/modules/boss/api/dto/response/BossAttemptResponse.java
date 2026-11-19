package com.dro.modules.boss.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record BossAttemptResponse(
        UUID id,
        String bossCode,
        String bossName,
        String status,
        int xpGained,
        int bitsGained,
        Instant createdAt
) {}
