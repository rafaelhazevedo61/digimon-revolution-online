package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.ToggleEquipmentLockRequest;
import com.dro.modules.equipment.api.dto.response.ToggleEquipmentLockResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Persiste a proteção contra consumo acidental de uma peça. */
@Service
public class ToggleEquipmentLockUseCase {
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public ToggleEquipmentLockResponse execute(String authorization, ToggleEquipmentLockRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        Equipment equipment = equipmentRepository.findByIdForUpdate(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId())) {
            throw new ConflictException("Equipment does not belong to this player");
        }
        equipment.setLocked(request.locked());
        equipmentRepository.save(equipment);
        return new ToggleEquipmentLockResponse(equipment.getId(), equipment.isLocked());
    }

    public ToggleEquipmentLockUseCase(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }
}
