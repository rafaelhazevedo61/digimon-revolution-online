package com.dro.modules.evolution.api.dto.response;

import java.util.List;

public record AvailableEvolutionLineResponse(
        String code,
        String name,
        String description,
        List<AvailableEvolutionLineStepResponse> steps
) {
}
