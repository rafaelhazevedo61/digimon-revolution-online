package com.dro.modules.digimon.api.dto.response;

import java.util.Map;

public record TraitHatchSimulationResponse(
        int attempts,
        int withTrait,
        int withoutTrait,
        double traitRate,
        Map<String, Integer> traitDistribution
) {
}