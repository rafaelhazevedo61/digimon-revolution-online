package com.dro.modules.mission.api.request;

import jakarta.validation.constraints.NotBlank;

public record StartMissionRequest(
        @NotBlank
        String missionId
) {}