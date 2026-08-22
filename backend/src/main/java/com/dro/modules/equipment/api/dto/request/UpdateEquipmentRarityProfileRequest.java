package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Payload administrativo para atualizar um perfil de raridade. */
public record UpdateEquipmentRarityProfileRequest(
        @NotNull @Min(0) Integer commonPercent,
        @NotNull @Min(0) Integer rarePercent,
        @NotNull @Min(0) Integer epicPercent,
        @NotNull @Min(0) Integer legendaryPercent
) {
}
