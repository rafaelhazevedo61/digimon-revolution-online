package com.dro.modules.equipment.api.dto.response;

import com.dro.modules.equipment.domain.Equipment;
import java.util.List;
import java.util.UUID;

/** Resultado do aprimoramento e identificadores consumidos. */
public record EnhanceEquipmentResponse(
        EquipmentResponse equipment,
        int previousTier,
        int currentTier,
        String coreCode,
        List<UUID> consumedEquipmentIds
) {
    public static EnhanceEquipmentResponse from(Equipment equipment, int previousTier, String coreCode, List<UUID> consumedEquipmentIds) {
        return new EnhanceEquipmentResponse(EquipmentResponse.from(equipment), previousTier, equipment.getTier(), coreCode, consumedEquipmentIds);
    }
}
