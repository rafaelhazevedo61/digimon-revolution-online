package com.dro.modules.evolution.api.dto.response;

import java.util.List;

public record EvolutionRequirementsResponse(
        int level,
        List<EvolutionMaterialRequirementResponse> materials
) {
}
