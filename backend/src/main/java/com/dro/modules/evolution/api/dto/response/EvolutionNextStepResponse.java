package com.dro.modules.evolution.api.dto.response;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionNextStepResponse(
        Long digimonInfoId,
        String name,
        String stage,
        String attribute,
        String element,
        String specie,
        int baseHp,
        int baseAtk,
        int baseDef,
        String imageUrl
) {
}
