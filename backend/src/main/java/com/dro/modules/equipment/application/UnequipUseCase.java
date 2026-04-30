package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnequipUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token, UUID equipmentId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!equipment.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Equipment does not belong to this player");
        }

        if (!equipment.isEquipped()) {
            throw new RuntimeException("Equipment is not equipped");
        }

        Digimon digimon = digimonRepository.findByPlayerId(playerId).stream()
                .filter(d -> equipment.getId().equals(d.getEquipmentIdBySlot(equipment.getSlot())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Digimon with this equipment not found"));

        digimon.clearSlot(equipment.getSlot());
        equipment.unequip();

        digimonRepository.save(digimon);
        equipmentRepository.save(equipment);
    }
}
