package com.dro.modules.mission.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Missões.
 */
public record MissionStartResponse(
        UUID missionInstanceId,
        Instant endsAt
) {}