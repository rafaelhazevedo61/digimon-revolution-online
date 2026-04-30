package com.dro.modules.equipment.application;

import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentTemplate;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrantEquipmentUseCase {

    private final EquipmentRepository equipmentRepository;

    public UUID execute(UUID playerId, String templateName) {

        EquipmentTemplate template = EquipmentTemplate.findByName(templateName);

        Equipment equipment = Equipment.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .digimonId(null)
                .name(template.getName())
                .slot(template.getSlot())
                .rarity(template.getRarity())
                .bonusHp(template.getBonusHp())
                .bonusAttack(template.getBonusAttack())
                .bonusDefense(template.getBonusDefense())
                .createdAt(LocalDateTime.now())
                .build();

        equipmentRepository.save(equipment);

        return equipment.getId();
    }
}
