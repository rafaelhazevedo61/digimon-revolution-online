package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
public record RefineEquipmentRequest(
        @NotNull UUID equipmentId,
        String successBoostItemCode,
        String protectionItemCode
) {
}
