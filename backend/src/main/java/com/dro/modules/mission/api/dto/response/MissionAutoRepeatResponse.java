package com.dro.modules.mission.api.dto.response;

import java.util.UUID;

/**
 * Estado atualizado da auto-repetição de uma instância.
 */
public record MissionAutoRepeatResponse(
        UUID missionInstanceId,
        int slotNumber,
        boolean enabled
) {
}
