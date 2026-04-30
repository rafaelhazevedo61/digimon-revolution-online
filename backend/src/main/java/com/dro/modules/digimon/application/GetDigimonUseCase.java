package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonUseCase {

    private final DigimonRepository digimonRepository;
    private final EquipmentRepository equipmentRepository;

    public List<DigimonResponse> execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var digimons = digimonRepository.findByPlayerId(playerId);

        return digimons.stream()
                .filter(d -> d.getStatus() == DigimonStatus.ACTIVE)
                .map(d -> {
                    List<Equipment> equipped = getEquippedItems(d);

                    return new DigimonResponse(
                            d.getId(),
                            d.getName(),
                            d.getType(),
                            d.getStage(),
                            d.getLevel(),
                            d.getExperience(),
                            d.getHp(),
                            d.getAttack(),
                            d.getDefense(),
                            d.getIvHp(),
                            d.getIvAttack(),
                            d.getIvDefense(),
                            d.getRarity(),
                            d.getPersonality(),
                            d.getTrait(),
                            d.getEnergy(),
                            d.getMaxEnergy(),
                            d.getBits(),
                            d.getRebirthCount(),
                            d.getRebornedFrom(),
                            d.getStatus(),
                            EquipmentRules.totalBonusHp(equipped),
                            EquipmentRules.totalBonusAttack(equipped),
                            EquipmentRules.totalBonusDefense(equipped)
                    );
                })
                .toList();
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
