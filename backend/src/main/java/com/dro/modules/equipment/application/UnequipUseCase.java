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

        if (!equipment.isEquipped()) {
            throw new RuntimeException("Equipment is not equipped");
        }

        Digimon digimon = digimonRepository.findById(equipment.getDigimonId())
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Digimon does not belong to this player");
        }

        digimon.clearSlot(equipment.getSlot());
        equipment.unequip();

        digimonRepository.save(digimon);
        equipmentRepository.save(equipment);
    }
}
