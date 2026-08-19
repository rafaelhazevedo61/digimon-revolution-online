package com.dro.modules.evolution.api.dto.response;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record AvailableEvolutionLineStepResponse(
        int order,
        Long digimonInfoId,
        String digimon,
        String stage,
        String attribute,
        String element,
        String specie
) {
}