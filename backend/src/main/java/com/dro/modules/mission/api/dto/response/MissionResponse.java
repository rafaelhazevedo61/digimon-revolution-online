package com.dro.modules.mission.api.dto.response;

public record MissionResponse(
        String id,
        String name,
        String description,
        String area,
        int requiredLevel,
        int xpReward,
        int energyCost,
        int durationSeconds
) {}