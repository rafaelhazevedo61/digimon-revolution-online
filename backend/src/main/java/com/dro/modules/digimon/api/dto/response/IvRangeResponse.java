package com.dro.modules.digimon.api.dto.response;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record IvRangeResponse(
        int min,
        int max
) {
}