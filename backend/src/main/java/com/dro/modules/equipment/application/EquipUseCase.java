package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    public void execute(String token, UUID equipmentId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipment not found"));

        if (!equipment.getDigimonId().equals(digimon.getId())) {
            throw new ForbiddenException("Equipment does not belong to this Digimon");
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
