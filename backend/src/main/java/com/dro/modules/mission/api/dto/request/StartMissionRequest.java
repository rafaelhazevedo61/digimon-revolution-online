package com.dro.modules.mission.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Contrato de dados do módulo de Missões.
 */
public record StartMissionRequest(
        @NotBlank
        String missionId
) {}