package com.dro.modules.mission.api.dto.response;

/**
 * Contrato de dados do módulo de Missões.
 */
public record MissionResponse(
        String id,
        String name,
        String description,
        String area,
        int requiredLevel,
        int xpReward,
        int bitsReward,
        int energyCost,
        int durationSeconds
) {}