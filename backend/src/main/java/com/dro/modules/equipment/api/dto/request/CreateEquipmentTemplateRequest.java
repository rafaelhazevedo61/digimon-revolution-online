package com.dro.modules.equipment.api.dto.request;

import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEquipmentTemplateRequest(
        @NotBlank String name,
        @NotBlank String setCode,
        @Min(1) int tier,
        @NotNull EquipmentSlot slot,
        @NotNull EquipmentRarity rarity,
        @Min(0) int bonusHp,
        @Min(0) int bonusAttack,
        @Min(0) int bonusDefense
) {
}
