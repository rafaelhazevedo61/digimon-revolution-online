package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token, UUID equipmentId, UUID digimonId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!equipment.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Equipment does not belong to this player");
        }

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Digimon does not belong to this player");
        }

        EquipmentRules.validateEquip(equipment);

        UUID currentEquipmentId = digimon.getEquipmentIdBySlot(equipment.getSlot());
        if (currentEquipmentId != null) {
            Equipment currentEquipment = equipmentRepository.findById(currentEquipmentId)
                    .orElse(null);
            if (currentEquipment != null) {
                currentEquipment.unequip();
                equipmentRepository.save(currentEquipment);
            }
        }

        equipment.equip();
        digimon.setEquipmentBySlot(equipment.getSlot(), equipment.getId());

        equipmentRepository.save(equipment);
        digimonRepository.save(digimon);
    }
}
