package com.dro.modules.equipment.api.dto.response;

import java.util.List;
import java.util.UUID;

public record DigimonEquipmentResponse(
        UUID digimonId,
        List<EquipmentResponse> equippedItems,
        int totalBonusHp,
        int totalBonusAttack,
        int totalBonusDefense,
        SetBonusResponse setBonus
) {
    public record SetBonusResponse(String setCode, int pieceCount, int bonusHpPercent, int bonusAtkPercent, int bonusDefPercent) {}
}
