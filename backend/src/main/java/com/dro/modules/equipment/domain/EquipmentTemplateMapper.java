package com.dro.modules.equipment.domain;

public class EquipmentTemplateMapper {

    private EquipmentTemplateMapper() {}

    public static EquipmentTemplate toTemplate(EquipmentTemplateEntity entity) {
        return new EquipmentTemplate(
                entity.getName(),
                entity.getSlot(),
                entity.getRarity(),
                entity.getSetCode(),
                entity.getTier(),
                entity.getBonusHp(),
                entity.getBonusAttack(),
                entity.getBonusDefense()
        );
    }
}
