package com.dro.modules.evolution.api.dto.response;

import java.util.List;

public record EvolutionLineResponse(
        Long id,
        String code,
        String name,
        boolean active,
        List<EvolutionLineStepResponse> steps
) {
}