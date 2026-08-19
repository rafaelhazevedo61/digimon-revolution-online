package com.dro.modules.equipment.api.dto.response;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
public record RefinePreviewResponse(
        int currentRefinementLevel,
        int nextRefinementLevel,
        int successRate,
        int costBits,
        int costStones,
        int currentBits,
        int currentStones,
        boolean canRefine
) {
}
