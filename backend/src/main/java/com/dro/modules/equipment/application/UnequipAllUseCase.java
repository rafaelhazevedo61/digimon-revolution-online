package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnequipAllUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    public int execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        int count = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipId = digimon.getEquipmentIdBySlot(slot);
            if (equipId != null) {
                Equipment equipment = equipmentRepository.findById(equipId).orElse(null);
                if (equipment != null) {
                    equipment.unequip();
                    equipmentRepository.save(equipment);
                }
                digimon.clearSlot(slot);
                count++;
            }
        }

        digimonRepository.save(digimon);

        return count;
    }
}
