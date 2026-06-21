package com.dro.modules.equipment.application;

import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentTemplate;
import com.dro.modules.equipment.domain.EquipmentTemplateMapper;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrantEquipmentUseCase {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;

    public UUID execute(UUID digimonId, String templateName) {

        EquipmentTemplate template = equipmentTemplateRepository.findByName(templateName)
                .map(EquipmentTemplateMapper::toTemplate)
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + templateName));

        Equipment equipment = Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .name(template.getName())
                .slot(template.getSlot())
                .rarity(template.getRarity())
                .setCode(template.getSetCode())
                .tier(template.getTier())
                .bonusHp(template.getBonusHp())
                .bonusAttack(template.getBonusAttack())
                .bonusDefense(template.getBonusDefense())
                .createdAt(LocalDateTime.now())
                .build();

        equipmentRepository.save(equipment);

        return equipment.getId();
    }
}
