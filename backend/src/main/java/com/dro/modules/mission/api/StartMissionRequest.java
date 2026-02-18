package com.dro.modules.mission.api;

import com.dro.modules.mission.domain.MissionType;
import jakarta.validation.constraints.NotNull;

public record StartMissionRequest(
        @NotNull
        MissionType type
) {}