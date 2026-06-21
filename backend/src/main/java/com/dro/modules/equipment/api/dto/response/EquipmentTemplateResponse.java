package com.dro.modules.equipment.api.dto.response;

import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;

import java.time.LocalDateTime;

public record EquipmentTemplateResponse(
        Long id,
        String name,
        String setCode,
        int tier,
        EquipmentSlot slot,
        EquipmentRarity rarity,
        int bonusHp,
        int bonusAttack,
        int bonusDefense,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
    public static EquipmentTemplateResponse from(EquipmentTemplateEntity entity) {
        return new EquipmentTemplateResponse(
                entity.getId(),
                entity.getName(),
                entity.getSetCode(),
                entity.getTier(),
                entity.getSlot(),
                entity.getRarity(),
                entity.getBonusHp(),
                entity.getBonusAttack(),
                entity.getBonusDefense(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
}
