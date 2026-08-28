package com.dro.modules.mission.api.dto.response;

import java.util.List;

public record NewlyUnlockedContentResponse(
        List<NewlyUnlockedMissionResponse> missions,
        List<NewlyUnlockedAreaResponse> areas
) {
    public static NewlyUnlockedContentResponse empty() {
        return new NewlyUnlockedContentResponse(List.of(), List.of());
    }

    public boolean hasAny() {
        return !missions.isEmpty() || !areas.isEmpty();
    }
}
