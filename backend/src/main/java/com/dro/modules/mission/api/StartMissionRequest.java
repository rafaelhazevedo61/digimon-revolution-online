package com.dro.modules.mission.api;

import jakarta.validation.constraints.NotBlank;

public record StartMissionRequest(
        @NotBlank
        String missionId
) {}