package com.dro.modules.mission.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MissionStartResponse(
        UUID missionInstanceId,
        Instant endsAt
) {}