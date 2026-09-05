package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.EnhanceEquipmentRequest;
import com.dro.modules.equipment.api.dto.response.EnhanceEquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentEnhancementRules;
import com.dro.modules.equipment.domain.EquipmentTemplate;
import com.dro.modules.equipment.domain.EquipmentTemplateMapper;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Consome três cópias exatas e um núcleo para elevar um equipamento em um tier. */
@Service
public class EnhanceEquipmentUseCase {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @Transactional
    public EnhanceEquipmentResponse execute(String authorization, EnhanceEquipmentRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        if (request.materialEquipmentIds().contains(request.equipmentId())
                || new HashSet<>(request.materialEquipmentIds()).size() != request.materialEquipmentIds().size()) {
            throw new ConflictException("Enhancement requires distinct equipment copies");
        }

        List<UUID> ids = new ArrayList<>();
        ids.add(request.equipmentId());
        ids.addAll(request.materialEquipmentIds());
        ids.sort(Comparator.comparing(UUID::toString));
        List<Equipment> locked = ids.stream()
                .map(id -> equipmentRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new NotFoundException("Equipment not found: " + id)))
                .toList();
        Equipment target = locked.stream().filter(e -> e.getId().equals(request.equipmentId())).findFirst().orElseThrow();
        int previousTier = target.getTier();
        int nextTier = EquipmentEnhancementRules.nextTier(previousTier);
        int requiredCopies = EquipmentEnhancementRules.requiredCopiesForTargetTier(nextTier);
        if (request.materialEquipmentIds().size() != requiredCopies - 1) {
            throw new ConflictException("This enhancement requires " + requiredCopies + " total equipment copies");
        }
        validateCopies(playerId, target, locked);
        String coreCode = EquipmentEnhancementRules.requiredCoreCode(nextTier);
        InventoryItem core = itemDefinitionRepository.findByCode(coreCode)
                .flatMap(definition -> inventoryRepository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, definition.getId()))
                .orElseThrow(() -> new UnprocessableException("Required enhancement core is missing: " + coreCode));
        if (core.getQuantity() < 1) {
            throw new UnprocessableException("Not enough enhancement cores: " + coreCode);
        }

        EquipmentTemplate nextTemplate = equipmentTemplateRepository
                .findBySetCodeAndSlotAndTier(target.getSetCode(), target.getSlot(), nextTier)
                .map(EquipmentTemplateMapper::toTemplate)
                .orElseThrow(() -> new NotFoundException("Next-tier equipment template not found"));

        core.setQuantity(core.getQuantity() - 1);
        if (core.getQuantity() == 0) inventoryRepository.delete(core); else inventoryRepository.save(core);
        equipmentRepository.deleteAllById(request.materialEquipmentIds());
        applyTemplate(target, nextTemplate, nextTier);
        equipmentRepository.save(target);

        return EnhanceEquipmentResponse.from(target, previousTier, coreCode, List.copyOf(request.materialEquipmentIds()));
    }

    private void validateCopies(UUID playerId, Equipment target, List<Equipment> copies) {
        for (Equipment equipment : copies) {
            if (!playerId.equals(equipment.getPlayerId())) throw new ConflictException("All equipment must belong to the same player");
            if (equipment.isEquipped() || equipment.getDigimonId() != null) throw new ConflictException("Equipped equipment cannot be used as enhancement material");
            if (equipment.isLocked()) throw new ConflictException("Locked equipment cannot be used as enhancement material");
            if (!target.getName().equals(equipment.getName())
                    || target.getSetCode() == null || !target.getSetCode().equals(equipment.getSetCode())
                    || target.getSlot() != equipment.getSlot()
                    || target.getTier() != equipment.getTier()
                    || target.getRarity() != equipment.getRarity()) {
                throw new ConflictException("Enhancement requires exact copies of the same equipment");
            }
        }
    }

    private void applyTemplate(Equipment target, EquipmentTemplate template, int nextTier) {
        target.setName(template.getName());
        target.setSlot(template.getSlot());
        target.setSetCode(template.getSetCode());
        target.setTier(nextTier);
        target.setBonusHp(template.getBonusHp());
        target.setBonusAttack(template.getBonusAttack());
        target.setBonusDefense(template.getBonusDefense());
        target.setDigimonId(null);
        target.setEquipped(false);
        target.setRefinementLevel(0);
        target.setAscensionLevel(0);
        target.setCreatedAt(LocalDateTime.now());
    }

    public EnhanceEquipmentUseCase(EquipmentRepository equipmentRepository,
                                   EquipmentTemplateRepository equipmentTemplateRepository,
                                   InventoryRepository inventoryRepository,
                                   ItemDefinitionRepository itemDefinitionRepository) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentTemplateRepository = equipmentTemplateRepository;
        this.inventoryRepository = inventoryRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
