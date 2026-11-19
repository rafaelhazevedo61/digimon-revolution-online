package com.dro.modules.evolution.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionOptionResponse(
        Long evolutionLineId,
        String evolutionLineCode,
        String evolutionLineName,
        EvolutionNextStepResponse nextStep,
        EvolutionRequirementsResponse requirements,
        boolean canEvolve,
        String reason
) {
}
