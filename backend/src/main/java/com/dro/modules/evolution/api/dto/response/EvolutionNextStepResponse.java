package com.dro.modules.evolution.api.dto.response;

public record EvolutionNextStepResponse(
        Long digimonInfoId,
        String name,
        String stage,
        String attribute,
        String element,
        String specie,
        int baseHp,
        int baseAtk,
        int baseDef
) {
}
