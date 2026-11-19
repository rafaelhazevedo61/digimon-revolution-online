package com.dro.modules.evolution.api.dto.response;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionLineStepMaterialResponse(
        Long itemDefinitionId,
        String itemCode,
        String itemName,
        Integer quantity
) {}