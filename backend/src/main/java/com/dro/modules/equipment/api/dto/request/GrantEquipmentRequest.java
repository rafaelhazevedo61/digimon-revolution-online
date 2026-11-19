package com.dro.modules.equipment.api.dto.request;

import com.dro.modules.equipment.domain.EquipmentRarity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
public record GrantEquipmentRequest(
        @NotNull UUID playerId,
        @NotBlank String templateName,
        EquipmentRarity rarity
) {
}
