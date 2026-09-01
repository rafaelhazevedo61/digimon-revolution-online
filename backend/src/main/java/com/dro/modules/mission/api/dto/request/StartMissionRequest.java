package com.dro.modules.mission.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Missões.
 */
public record StartMissionRequest(
        @NotBlank
        String missionId,
        UUID teamId,
        boolean autoRepeat
) {
    public StartMissionRequest(String missionId, UUID teamId) {
        this(missionId, teamId, false);
    }
}
