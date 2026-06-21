package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RefineEquipmentRequest(
        @NotNull UUID equipmentId
) {
}
