package com.dro.modules.equipment.api.dto.request;

import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateEquipmentTemplateRequest(
        @NotNull EquipmentSlot slot,
        String setCode,
        Integer tier,
        EquipmentRarity rarity,
        @Min(0) int bonusHp,
        @Min(0) int bonusAttack,
        @Min(0) int bonusDefense
) {
}
