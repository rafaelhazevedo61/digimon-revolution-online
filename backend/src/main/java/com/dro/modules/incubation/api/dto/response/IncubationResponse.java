package com.dro.modules.incubation.api.dto.response;

import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.inventory.domain.ItemType;

import java.time.LocalDateTime;

/**
 * Contrato de dados do módulo de Incubação.
 */
public record IncubationResponse(
        ItemType digitamaType,
        ItemType incubatorType,
        IncubationStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishAt,
        long remainingSeconds
) {}
