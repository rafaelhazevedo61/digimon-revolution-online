package com.dro.modules.evolution.api.dto.response;

public record EvolutionMaterialRequirementResponse(
        String materialCode,
        String description,
        int quantity,
        int playerHas
) {
}
