package com.dro.modules.equipment.api.dto.response;

import java.util.UUID;

public record ToggleEquipmentLockResponse(UUID equipmentId, boolean locked) {
}
