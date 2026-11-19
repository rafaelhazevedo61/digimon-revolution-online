package com.dro.modules.equipment.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Equipamentos.
 */
public record GrantEquipmentResponse(
        UUID equipmentId,
        String message
) {
}
