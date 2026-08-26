package com.dro.modules.incubation.api.dto.response;

import java.util.List;

/**
 * Estado completo da incubadora do jogador.
 */
public record IncubationSlotsResponse(
        int totalSlots,
        int unlockedSlots,
        List<IncubationSlotResponse> slots
) {
}
