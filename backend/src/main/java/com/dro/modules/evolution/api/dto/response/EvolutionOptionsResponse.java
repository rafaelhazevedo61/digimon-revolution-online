package com.dro.modules.evolution.api.dto.response;

import java.util.List;

public record EvolutionOptionsResponse(
        Long currentDigimonInfoId,
        String currentName,
        String currentStage,
        int currentLevel,
        List<EvolutionOptionResponse> options
) {
}
