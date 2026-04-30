package com.dro.modules.equipment.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonEquipmentUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;

    public DigimonEquipmentResponse execute(String token, UUID digimonId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        var digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Digimon does not belong to this player");
        }

        List<Equipment> equipped = equipmentRepository.findByDigimonId(digimonId);

        List<EquipmentResponse> items = equipped.stream()
                .map(EquipmentResponse::from)
                .toList();

        return new DigimonEquipmentResponse(
                digimonId,
                items,
                EquipmentRules.totalBonusHp(equipped),
                EquipmentRules.totalBonusAttack(equipped),
                EquipmentRules.totalBonusDefense(equipped)
        );
    }
}
