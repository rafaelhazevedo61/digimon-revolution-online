package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonByIdUseCase {

    private final DigimonRepository digimonRepository;
    private final EquipmentRepository equipmentRepository;

    public DigimonResponse execute(UUID digimonId) {

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        List<Equipment> equipped = getEquippedItems(digimon);

        return new DigimonResponse(
                digimon.getId(),
                digimon.getName(),
                digimon.getType(),
                digimon.getStage(),
                digimon.getLevel(),
                digimon.getExperience(),
                digimon.getHp(),
                digimon.getAttack(),
                digimon.getDefense(),
                digimon.getIvHp(),
                digimon.getIvAttack(),
                digimon.getIvDefense(),
                digimon.getGrade(),
                digimon.getRarity(),
                digimon.getPersonality(),
                digimon.getTrait(),
                digimon.getEnergy(),
                digimon.getMaxEnergy(),
                digimon.getBits(),
                digimon.getRebirthCount(),
                digimon.getRebornedFrom(),
                digimon.getStatus(),
                EquipmentRules.totalBonusHp(equipped),
                EquipmentRules.totalBonusAttack(equipped),
                EquipmentRules.totalBonusDefense(equipped)
        );
    }

    private List<Equipment> getEquippedItems(Digimon digimon) {
        List<Equipment> equipped = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipId = digimon.getEquipmentIdBySlot(slot);
            if (equipId != null) {
                equipmentRepository.findById(equipId).ifPresent(equipped::add);
            }
        }

        return equipped;
    }
}
