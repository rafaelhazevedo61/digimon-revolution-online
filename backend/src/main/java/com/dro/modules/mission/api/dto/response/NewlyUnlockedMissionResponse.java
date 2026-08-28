package com.dro.modules.mission.api.dto.response;

public record NewlyUnlockedMissionResponse(
        String id,
        String name,
        String area,
        int requiredLevel
) {
}
