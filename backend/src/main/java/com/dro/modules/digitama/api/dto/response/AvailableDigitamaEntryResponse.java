package com.dro.modules.digitama.api.dto.response;

/**
 * Contrato de dados do módulo de Digitama.
 */
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