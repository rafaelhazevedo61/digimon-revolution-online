package com.dro.modules.equipment.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonInventoryUseCase {

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;

    public List<EquipmentResponse> execute(String token, UUID digimonId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("Digimon does not belong to this player");
        }

        List<Equipment> items = equipmentRepository.findByDigimonId(digimonId);

        return items.stream()
                .map(EquipmentResponse::from)
                .toList();
    }
}
