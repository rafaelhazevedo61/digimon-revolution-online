package com.dro.modules.equipment.api.dto.response;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
public record RefinePreviewResponse(
        int currentRefinementLevel,
        int nextRefinementLevel,
        int baseSuccessRate,
        int successRate,
        int breakChance,
        int costBits,
        int costStones,
        int currentBits,
        int currentStones,
        int successBoostItemCount,
        int protectionItemCount,
        boolean canRefine
) {
}
