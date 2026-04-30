package com.dro.modules.equipment.api.dto.response;

import java.util.UUID;

public record GrantEquipmentResponse(
        UUID equipmentId,
        String message
) {
}
