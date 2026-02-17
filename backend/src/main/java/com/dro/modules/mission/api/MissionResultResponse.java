package com.dro.modules.mission.api;

public record MissionResultResponse(
        int xpGained,
        int newLevel
) {}