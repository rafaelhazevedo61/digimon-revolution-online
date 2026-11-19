package com.dro.modules.evolution.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionRequirementsResponse(
        int level,
        List<EvolutionMaterialRequirementResponse> materials
) {
}
