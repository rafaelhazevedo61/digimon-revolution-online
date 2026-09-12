package com.dro.modules.loot.application;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.api.dto.response.ChestLootPreviewResponse;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class GetChestLootPreviewUseCase {
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @Transactional(readOnly = true)
    public ChestLootPreviewResponse execute(String code) {
        ChestDefinitionEntity chest = chestDefinitionRepository.findWithCatalogByCode(code)
                .filter(ChestDefinitionEntity::isActive)
                .orElseThrow(() -> new NotFoundException("Baú não encontrado: " + code));
        LootTableEntity lootTable = chest.getLootTable();
        if (lootTable == null || !lootTable.isActive()) {
            return new ChestLootPreviewResponse(chest.getCode(), chest.getName(), chest.getDescription(), chest.getIcon(), 0, 0, java.util.List.of(), java.util.List.of());
        }

        Map<String, ItemDefinition> itemDefinitions = new HashMap<>();
        var rarityWeights = lootTable.getRarityWeights().stream()
                .map(weight -> new ChestLootPreviewResponse.RarityWeight(weight.getRarity().name(), weight.getWeight()))
                .toList();
        var items = lootTable.getEntries().stream()
                .filter(entry -> entry.isActive())
                .map(entry -> {
                    String itemType = entry.getItemType().name();
                    String itemCode = entry.getEquipmentTemplateName() != null
                            ? entry.getEquipmentTemplateName()
                            : entry.getMaterialCode() == null || entry.getMaterialCode().isBlank()
                            ? itemType
                            : entry.getMaterialCode();
                    ItemDefinition definition = itemDefinitions.computeIfAbsent(itemCode,
                            key -> itemDefinitionRepository.findByCode(key).orElse(null));
                    String itemName = definition != null && definition.getName() != null ? definition.getName() : itemCode;
                    return new ChestLootPreviewResponse.LootItem(
                            entry.getRarity().name(), itemType, itemCode, itemName,
                            entry.getWeight(), entry.getMinQuantity(), entry.getMaxQuantity(),
                            entry.getEquipmentTemplateName(), entry.getEquipmentRarity() == null ? null : entry.getEquipmentRarity().name()
                    );
                })
                .toList();
        return new ChestLootPreviewResponse(chest.getCode(), chest.getName(), chest.getDescription(), chest.getIcon(),
                lootTable.getMinItems(), lootTable.getMaxItems(), rarityWeights, items);
    }

    public GetChestLootPreviewUseCase(ChestDefinitionRepository chestDefinitionRepository,
                                      ItemDefinitionRepository itemDefinitionRepository) {
        this.chestDefinitionRepository = chestDefinitionRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
