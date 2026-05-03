package com.dro.modules.digitama.api.dto.response;

public record AvailableDigitamaEntryResponse(
        Long digimonInfoId,
        String digimonName,
        String stage,
        String attribute,
        String element,
        String specie,
        int weight
) {
}