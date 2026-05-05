package com.dro.modules.evolution.api.dto.response;

public record EvolutionLineStepMaterialResponse(
        Long itemDefinitionId,
        String itemCode,
        String itemName,
        Integer quantity
) {}