package com.dro.modules.digimon.api.dto.response;

import java.util.Map;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record TraitHatchSimulationResponse(
        int attempts,
        int withTrait,
        int withoutTrait,
        double traitRate,
        Map<String, Integer> traitDistribution
) {
}