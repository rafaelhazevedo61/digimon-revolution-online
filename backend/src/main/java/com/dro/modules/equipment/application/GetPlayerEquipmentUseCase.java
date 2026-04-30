package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerEquipmentUseCase {

    private final EquipmentRepository equipmentRepository;

    public List<EquipmentResponse> execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        List<Equipment> items = equipmentRepository.findByPlayerId(playerId);

        return items.stream()
                .map(EquipmentResponse::from)
                .toList();
    }
}
