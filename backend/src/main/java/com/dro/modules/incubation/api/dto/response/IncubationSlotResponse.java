package com.dro.modules.incubation.api.dto.response;

/**
 * Estado visual e operacional de uma posição da incubadora.
 */
public record IncubationSlotResponse(
        int slotNumber,
        boolean unlocked,
        IncubationResponse incubation
) {
}
