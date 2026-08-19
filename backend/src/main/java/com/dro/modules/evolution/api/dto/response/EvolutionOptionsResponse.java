package com.dro.modules.evolution.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionOptionsResponse(
        Long currentDigimonInfoId,
        String currentName,
        String currentStage,
        int currentLevel,
        String currentAttribute,
        String currentElement,
        List<EvolutionOptionResponse> options
) {
}
