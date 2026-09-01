package com.dro.modules.mission.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Contrato exclusivo para o reenvio automático de uma missão com time.
 */
public record StartAutoMissionRequest(
        @NotBlank
        String missionId,
        UUID teamId
) {
}
