package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Identifica o equipamento não equipado que será desmontado. */
public record DismantleEquipmentRequest(@NotNull UUID equipmentId) {
}
