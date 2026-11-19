package com.dro.modules.evolution.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record AvailableEvolutionLineResponse(
        String code,
        String name,
        String description,
        List<AvailableEvolutionLineStepResponse> steps
) {
}
