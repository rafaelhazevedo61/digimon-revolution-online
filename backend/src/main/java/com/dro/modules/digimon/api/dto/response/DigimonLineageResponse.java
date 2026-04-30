package com.dro.modules.digimon.api.dto.response;

import java.util.List;
import java.util.UUID;

public record DigimonLineageResponse(
        UUID currentDigimonId,
        int totalGenerations,
        List<DigimonLineageItemResponse> lineage
) {
}