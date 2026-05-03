package com.dro.modules.evolution.api.dto.response;

import java.util.List;

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
