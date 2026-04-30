package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipRequest(
        @NotNull UUID equipmentId,
        @NotNull UUID digimonId
) {
}
