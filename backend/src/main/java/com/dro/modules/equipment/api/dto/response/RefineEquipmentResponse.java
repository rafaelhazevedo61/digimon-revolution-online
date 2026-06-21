package com.dro.modules.equipment.api.dto.response;

public record RefineEquipmentResponse(
        String message,
        boolean success,
        int newRefinementLevel,
        int successRate,
        int costBits,
        int costStones,
        EquipmentResponse equipment
) {
}
