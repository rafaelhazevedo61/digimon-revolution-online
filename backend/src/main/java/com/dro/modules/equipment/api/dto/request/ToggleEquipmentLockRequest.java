package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Define explicitamente o estado de bloqueio de um equipamento. */
public record ToggleEquipmentLockRequest(
        @NotNull UUID equipmentId,
        @NotNull Boolean locked
) {
}
