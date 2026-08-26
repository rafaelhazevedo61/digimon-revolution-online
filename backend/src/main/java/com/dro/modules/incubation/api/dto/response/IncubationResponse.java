package com.dro.modules.incubation.api.dto.response;

import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.inventory.domain.ItemType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados de uma incubação individual.
 */
public record IncubationResponse(
        UUID id,
        int slotNumber,
        ItemType digitamaType,
        ItemType incubatorType,
        IncubationStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishAt,
        long remainingSeconds
) {
}
