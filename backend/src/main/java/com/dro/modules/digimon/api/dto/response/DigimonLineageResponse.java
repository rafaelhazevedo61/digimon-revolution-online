package com.dro.modules.digimon.api.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record DigimonLineageResponse(
        UUID currentDigimonId,
        int totalGenerations,
        List<DigimonLineageItemResponse> lineage
) {
}