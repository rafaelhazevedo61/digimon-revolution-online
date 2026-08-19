package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.DigimonInfos;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record DigimonInfoResponse(
        Long id,
        String name,
        String stage,
        String attribute,
        String element,
        String specie,
        int baseHp,
        int baseAtk,
        int baseDef
) {
    public static DigimonInfoResponse from(DigimonInfos digimonInfo) {
        return new DigimonInfoResponse(
                digimonInfo.getId(),
                digimonInfo.getName(),
                digimonInfo.getStage().name(),
                digimonInfo.getAttribute().name(),
                digimonInfo.getElement().name(),
                digimonInfo.getSpecie().name(),
                digimonInfo.getBaseHp(),
                digimonInfo.getBaseAtk(),
                digimonInfo.getBaseDef()
        );
    }
}