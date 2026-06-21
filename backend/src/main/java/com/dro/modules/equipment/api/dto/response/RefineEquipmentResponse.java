package com.dro.modules.equipment.api.dto.response;

public record RefineEquipmentResponse(
        String message,
        int newRefinementLevel,
        int costBits,
        int costStones,
        EquipmentResponse equipment
) {
}
