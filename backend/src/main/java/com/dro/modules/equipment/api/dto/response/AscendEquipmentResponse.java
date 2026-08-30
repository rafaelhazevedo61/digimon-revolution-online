package com.dro.modules.equipment.api.dto.response;

public record AscendEquipmentResponse(
        String message,
        int ascensionLevel,
        int requiredRebirths,
        EquipmentResponse equipment
) {}
