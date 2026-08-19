package com.dro.modules.evolution.api.dto.response;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionMaterialRequirementResponse(
        String materialCode,
        String description,
        int quantity,
        int playerHas
) {
}
