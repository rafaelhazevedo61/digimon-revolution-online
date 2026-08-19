package com.dro.modules.evolution.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionLineResponse(
        Long id,
        String code,
        String name,
        boolean active,
        List<EvolutionLineStepResponse> steps
) {
}