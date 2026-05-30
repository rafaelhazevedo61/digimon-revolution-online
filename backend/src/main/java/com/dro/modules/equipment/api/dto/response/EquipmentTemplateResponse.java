package com.dro.modules.equipment.api.dto.response;

import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;

public record EquipmentTemplateResponse(
        String name,
        EquipmentSlot slot,
        EquipmentRarity rarity,
        int bonusHp,
        int bonusAttack,
        int bonusDefense,
        boolean active
) {
    public static EquipmentTemplateResponse from(EquipmentTemplateEntity entity) {
        return new EquipmentTemplateResponse(
                entity.getName(),
                entity.getSlot(),
                entity.getRarity(),
                entity.getBonusHp(),
                entity.getBonusAttack(),
                entity.getBonusDefense(),
                entity.isActive()
        );
    }
}
