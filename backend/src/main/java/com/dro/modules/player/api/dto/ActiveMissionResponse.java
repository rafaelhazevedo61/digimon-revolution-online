package com.dro.modules.player.api.dto;

import com.dro.modules.mission.domain.MissionStatus;

import java.time.Instant;
import java.util.UUID;

public record ActiveMissionResponse(
        UUID instanceId,
        String missionId,
        String missionName,
        MissionStatus status,
        Instant startedAt,
        Instant endsAt,
        long remainingSeconds
) {}
