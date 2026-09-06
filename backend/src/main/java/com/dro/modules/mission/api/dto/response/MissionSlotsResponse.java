package com.dro.modules.mission.api.dto.response;

import java.util.List;

/**
 * Capacidade e missões ocupando os slots paralelos do jogador.
 */
public record MissionSlotsResponse(
        int totalSlots,
        int unlockedSlots,
        List<MissionInstanceResponse> activeMissions
) {
}
