package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantEquipmentRequest(
        @NotNull UUID playerId,
        @NotBlank String templateName
) {
}
