package com.dro.modules.digitama.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Digitama.
 */
public record AvailableDigitamaPoolResponse(
        String code,
        String name,
        String description,
        List<AvailableDigitamaEntryResponse> entries
) {
}