package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.DismantleEquipmentRequest;
import com.dro.modules.equipment.api.dto.response.DismantleEquipmentBatchResponse;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Converte equipamentos não equipados em núcleos de aprimoramento. */
@Service
public class DismantleEquipmentUseCase {
    private final EquipmentRepository equipmentRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final AddItemUseCase addItemUseCase;

    @Transactional
    public DismantleEquipmentResponse execute(String authorization, DismantleEquipmentRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        BatchResult result = dismantle(playerId, List.of(request.equipmentId()));
        EquipmentEnhancementRules.DismantleReward reward = EquipmentEnhancementRules.dismantleReward(result.tiers().get(0));
        return new DismantleEquipmentResponse(request.equipmentId(), result.tiers().get(0), reward.coreCode(), reward.quantity());
    }

    @Transactional
    public DismantleEquipmentBatchResponse executeBatch(String authorization, List<UUID> equipmentIds) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        BatchResult result = dismantle(playerId, equipmentIds);
        return new DismantleEquipmentBatchResponse(result.ids().size(), result.cores(), result.ids());
    }

    private BatchResult dismantle(UUID playerId, List<UUID> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw new ConflictException("Select at least one equipment");
        }
        if (equipmentIds.stream().distinct().count() != equipmentIds.size()) {
            throw new ConflictException("Equipment IDs must be distinct");
        }
        List<UUID> sortedIds = new ArrayList<>(equipmentIds);
        sortedIds.sort(Comparator.comparing(UUID::toString));
        List<Equipment> equipment = sortedIds.stream()
                .map(id -> equipmentRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new NotFoundException("Equipment not found: " + id)))
                .toList();
        Map<String, Integer> cores = new LinkedHashMap<>();
        List<Integer> tiers = new ArrayList<>();
        for (Equipment item : equipment) {
            if (!playerId.equals(item.getPlayerId())) throw new ConflictException("Equipment does not belong to this player");
            if (item.isEquipped() || item.getDigimonId() != null) throw new ConflictException("Equipped equipment cannot be dismantled");
            if (item.isLocked()) throw new ConflictException("Locked equipment cannot be dismantled");
            EquipmentEnhancementRules.DismantleReward reward = EquipmentEnhancementRules.dismantleReward(item.getTier());
            cores.merge(reward.coreCode(), reward.quantity(), Integer::sum);
            tiers.add(item.getTier());
        }
        Map<String, ItemDefinition> definitions = new LinkedHashMap<>();
        for (String code : cores.keySet()) {
            definitions.put(code, itemDefinitionRepository.findByCode(code)
                    .orElseThrow(() -> new NotFoundException("Enhancement core definition not found: " + code)));
        }
        equipmentRepository.deleteAllById(equipmentIds);
        cores.forEach((code, quantity) -> addItemUseCase.addMaterialToPlayer(playerId, definitions.get(code), quantity));
        return new BatchResult(List.copyOf(equipmentIds), cores, tiers);
    }

    private record BatchResult(List<UUID> ids, Map<String, Integer> cores, List<Integer> tiers) { }

    public DismantleEquipmentUseCase(EquipmentRepository equipmentRepository,
                                     ItemDefinitionRepository itemDefinitionRepository,
                                     AddItemUseCase addItemUseCase) {
        this.equipmentRepository = equipmentRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.addItemUseCase = addItemUseCase;
    }
}
