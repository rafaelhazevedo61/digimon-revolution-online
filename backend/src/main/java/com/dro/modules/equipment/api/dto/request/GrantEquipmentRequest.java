package com.dro.modules.equipment.api.dto.request;

import com.dro.modules.equipment.domain.EquipmentRarity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantEquipmentRequest(
        @NotNull UUID digimonId,
        @NotBlank String templateName,
        EquipmentRarity rarity
) {
}
