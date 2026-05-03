package com.dro.modules.evolution.api.dto.response;

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