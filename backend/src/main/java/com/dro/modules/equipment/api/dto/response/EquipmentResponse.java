package com.dro.modules.equipment.api.dto.response;

import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;

import java.time.LocalDateTime;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        String name,
        EquipmentSlot slot,
        EquipmentRarity rarity,
        String setCode,
        int tier,
        int refinementLevel,
        int bonusHp,
        int bonusAttack,
        int bonusDefense,
        int effectiveBonusHp,
        int effectiveBonusAttack,
        int effectiveBonusDefense,
        boolean equipped,
        LocalDateTime createdAt
) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getName(),
                equipment.getSlot(),
                equipment.getRarity(),
                equipment.getSetCode(),
                equipment.getTier(),
                equipment.getRefinementLevel(),
                equipment.getBonusHp(),
                equipment.getBonusAttack(),
                equipment.getBonusDefense(),
                equipment.getEffectiveBonusHp(),
                equipment.getEffectiveBonusAttack(),
                equipment.getEffectiveBonusDefense(),
                equipment.isEquipped(),
                equipment.getCreatedAt()
        );
    }
}
