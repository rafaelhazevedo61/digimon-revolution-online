package com.dro.modules.equipment.api.dto.response;

import java.util.List;
import java.util.UUID;

public record DigimonEquipmentResponse(
        UUID digimonId,
        List<EquipmentResponse> equippedItems,
        int totalBonusHp,
        int totalBonusAttack,
        int totalBonusDefense
) {
}
