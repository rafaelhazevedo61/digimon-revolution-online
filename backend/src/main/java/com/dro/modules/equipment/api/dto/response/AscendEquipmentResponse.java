package com.dro.modules.equipment.api.dto.response;

public record AscendEquipmentResponse(
        String message,
        int ascensionLevel,
        EquipmentResponse equipment
) {}
