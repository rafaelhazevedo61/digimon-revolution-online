package com.dro.modules.evolution.api.dto.response;

import java.util.List;

public record EvolutionLineStepResponse(
        int stepOrder,
        Long digimonInfoId,
        String digimonName,
        String stage,
        Integer requiredLevel,
        List<EvolutionLineStepMaterialResponse> materials
) {
}