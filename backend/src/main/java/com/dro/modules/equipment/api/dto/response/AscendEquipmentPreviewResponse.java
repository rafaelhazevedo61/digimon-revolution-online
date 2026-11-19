package com.dro.modules.equipment.api.dto.response;

public record AscendEquipmentPreviewResponse(
        EquipmentResponse equipment,
        int nextAscensionLevel,
        int requiredRefinementLevel,
        int coreCost,
        int currentCores,
        int bitsCost,
        int currentBits,
        int beforeHp,
        int afterHp,
        int beforeAttack,
        int afterAttack,
        int beforeDefense,
        int afterDefense,
        boolean canAscend,
        String restrictionMessage
) {}
