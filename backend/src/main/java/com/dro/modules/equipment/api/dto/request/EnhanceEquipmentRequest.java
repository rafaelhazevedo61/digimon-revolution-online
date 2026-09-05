package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Equipamento alvo e as duas cópias consumidas no aprimoramento. */
public record EnhanceEquipmentRequest(
        @NotNull UUID equipmentId,
        @NotNull @Size(min = 2, max = 2) List<UUID> materialEquipmentIds
) {
}
