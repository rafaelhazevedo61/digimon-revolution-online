package com.dro.modules.equipment.api.dto.response;

public record RefinePreviewResponse(
        int currentRefinementLevel,
        int nextRefinementLevel,
        int costBits,
        int costStones,
        int currentBits,
        int currentStones,
        boolean canRefine
) {
}
