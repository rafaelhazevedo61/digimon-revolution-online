package com.dro.modules.mission.api.dto.response;

import com.dro.modules.mission.domain.MissionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Missões.
 */
public record MissionInstanceResponse(
        UUID missionInstanceId,
        String missionId,
        String missionName,
        MissionStatus status,
        Instant startedAt,
        Instant endsAt
) {}