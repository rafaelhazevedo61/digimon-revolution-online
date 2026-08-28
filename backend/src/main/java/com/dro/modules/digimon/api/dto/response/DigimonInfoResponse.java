package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.DigimonInfos;

import java.util.List;

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
        int baseDef,
        String imageUrl,
        List<DigitamaOriginResponse> digitamaOrigins
) {
    public static DigimonInfoResponse from(DigimonInfos digimonInfo) {
        return from(digimonInfo, List.of());
    }

    public static DigimonInfoResponse from(DigimonInfos digimonInfo, List<DigitamaOriginResponse> digitamaOrigins) {
        return new DigimonInfoResponse(
                digimonInfo.getId(),
                digimonInfo.getName(),
                digimonInfo.getStage().name(),
                digimonInfo.getAttribute().name(),
                digimonInfo.getElement().name(),
                digimonInfo.getSpecie().name(),
                digimonInfo.getBaseHp(),
                digimonInfo.getBaseAtk(),
                digimonInfo.getBaseDef(),
                digimonInfo.getImageUrl(),
                digitamaOrigins == null ? List.of() : List.copyOf(digitamaOrigins)
        );
    }
}