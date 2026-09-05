package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.DismantleEquipmentRequest;
import com.dro.modules.equipment.api.dto.response.DismantleEquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentEnhancementRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Converte um equipamento não equipado em núcleos de aprimoramento. */
@Service
public class DismantleEquipmentUseCase {
    private final EquipmentRepository equipmentRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final AddItemUseCase addItemUseCase;

    @Transactional
    public DismantleEquipmentResponse execute(String authorization, DismantleEquipmentRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        Equipment equipment = equipmentRepository.findByIdForUpdate(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId())) {
            throw new ConflictException("Equipment does not belong to this player");
        }
        if (equipment.isEquipped() || equipment.getDigimonId() != null) {
            throw new ConflictException("Equipped equipment cannot be dismantled");
        }

        EquipmentEnhancementRules.DismantleReward reward =
                EquipmentEnhancementRules.dismantleReward(equipment.getTier());
        ItemDefinition core = itemDefinitionRepository.findByCode(reward.coreCode())
                .orElseThrow(() -> new NotFoundException("Enhancement core definition not found: " + reward.coreCode()));

        equipmentRepository.delete(equipment);
        addItemUseCase.addMaterialToPlayer(playerId, core, reward.quantity());
        return new DismantleEquipmentResponse(equipment.getId(), equipment.getTier(), reward.coreCode(), reward.quantity());
    }

    public DismantleEquipmentUseCase(EquipmentRepository equipmentRepository,
                                      ItemDefinitionRepository itemDefinitionRepository,
                                      AddItemUseCase addItemUseCase) {
        this.equipmentRepository = equipmentRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.addItemUseCase = addItemUseCase;
    }
}
