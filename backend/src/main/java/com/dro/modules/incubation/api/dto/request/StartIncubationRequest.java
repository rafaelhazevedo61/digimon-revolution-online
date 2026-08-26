package com.dro.modules.incubation.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Incubação.
 */
public record StartIncubationRequest(
        @Min(1)
        @Max(3)
        int slotNumber,
        @NotNull
        ItemType digitamaType,
        @NotNull
        ItemType incubatorType
) {
}
