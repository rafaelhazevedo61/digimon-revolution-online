package com.dro.modules.mission.api;

public record MissionResponse(
        String id,
        String name,
        String description,
        int requiredLevel,
        int xpReward
) {}