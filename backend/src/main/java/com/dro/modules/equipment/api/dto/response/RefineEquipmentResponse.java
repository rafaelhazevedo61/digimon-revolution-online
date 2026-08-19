package com.dro.modules.equipment.api.dto.response;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
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
