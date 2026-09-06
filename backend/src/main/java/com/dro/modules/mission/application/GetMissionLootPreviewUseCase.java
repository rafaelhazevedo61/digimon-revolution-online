package com.dro.modules.mission.application;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.mission.api.dto.response.MissionLootPreviewResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta as recompensas que podem ser obtidas em uma missão ativa.
 */
@Service
public class GetMissionLootPreviewUseCase {
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @Transactional(readOnly = true)
    public MissionLootPreviewResponse execute(String token, String missionId) {
        TokenExtractor.extractPlayerId(token);
        MissionDefinitionEntity mission = missionDefinitionRepository.findByIdAndActiveTrue(missionId)
                .orElseThrow(() -> new NotFoundException("Mission not found"));

        Map<String, ItemDefinition> itemDefinitions = new HashMap<>();
        ChestDefinitionEntity chest = mission.getChestDefinition();
        List<MissionLootPreviewResponse.FixedReward> fixedRewards = chest == null
                ? mission.getRewards().stream()
                        .map(reward -> {
                            String itemType = reward.getItemType().name();
                            return new MissionLootPreviewResponse.FixedReward(
                                    itemType,
                                    itemType,
                                    resolveItemName(itemType, itemType, itemDefinitions),
                                    reward.getBaseQuantity()
                            );
                        })
                        .toList()
                : List.of();
        MissionLootPreviewResponse.ChestPreview chestPreview = chest == null
                ? null
                : toChestPreview(chest, itemDefinitions);

        List<MissionLootPreviewResponse.LootChance> lootChances = chest == null
                ? mission.getLootChances().stream()
                        .map(chance -> new MissionLootPreviewResponse.LootChance(
                                chance.getRarity().name(),
                                chance.getChance()
                        ))
                        .toList()
                : List.of();

        List<MissionLootPreviewResponse.LegacyLootItem> lootItems = chest == null
                ? mission.getLootItems().stream()
                        .map(item -> {
                            String itemType = item.getItemType().name();
                            String itemCode = itemType;
                            return new MissionLootPreviewResponse.LegacyLootItem(
                                    item.getRarity().name(),
                                    itemType,
                                    itemCode,
                                    resolveItemName(itemCode, itemType, itemDefinitions),
                                    item.getQuantity()
                            );
                        })
                        .toList()
                : List.of();

        return new MissionLootPreviewResponse(
                mission.getId(),
                mission.getName(),
                mission.getBaseXp(),
                mission.getBaseBits(),
                fixedRewards,
                chestPreview,
                lootChances,
                lootItems
        );
    }

    private MissionLootPreviewResponse.ChestPreview toChestPreview(
            ChestDefinitionEntity chest,
            Map<String, ItemDefinition> itemDefinitions
    ) {
        LootTableEntity lootTable = chest.getLootTable();
        if (lootTable == null) {
            return new MissionLootPreviewResponse.ChestPreview(
                    chest.getCode(),
                    chest.getName(),
                    chest.getDescription(),
                    chest.getIcon(),
                    0,
                    0,
                    List.of(),
                    List.of()
            );
        }

        List<MissionLootPreviewResponse.RarityWeight> rarityWeights = lootTable.getRarityWeights().stream()
                .map(weight -> new MissionLootPreviewResponse.RarityWeight(
                        weight.getRarity().name(),
                        weight.getWeight()
                ))
                .toList();

        List<MissionLootPreviewResponse.ChestLootItem> items = lootTable.getEntries().stream()
                .filter(entry -> entry.isActive())
                .map(entry -> {
                    String itemType = entry.getItemType().name();
                    String itemCode = entry.getEquipmentTemplateName() != null
                            ? entry.getEquipmentTemplateName()
                            : entry.getMaterialCode() == null || entry.getMaterialCode().isBlank()
                            ? itemType
                            : entry.getMaterialCode();
                    return new MissionLootPreviewResponse.ChestLootItem(
                            entry.getRarity().name(),
                            itemType,
                            itemCode,
                            resolveItemName(itemCode, itemType, itemDefinitions),
                            entry.getWeight(),
                            entry.getMinQuantity(),
                            entry.getMaxQuantity(),
                            entry.getEquipmentTemplateName(),
                            entry.getEquipmentRarity() == null ? null : entry.getEquipmentRarity().name()
                    );
                })
                .toList();

        return new MissionLootPreviewResponse.ChestPreview(
                chest.getCode(),
                chest.getName(),
                chest.getDescription(),
                chest.getIcon(),
                lootTable.getMinItems(),
                lootTable.getMaxItems(),
                rarityWeights,
                items
        );
    }

    private String resolveItemName(
            String itemCode,
            String fallback,
            Map<String, ItemDefinition> itemDefinitions
    ) {
        ItemDefinition itemDefinition = itemDefinitions.computeIfAbsent(
                itemCode,
                code -> itemDefinitionRepository.findByCode(code).orElse(null)
        );
        return itemDefinition != null && itemDefinition.getName() != null
                ? itemDefinition.getName()
                : fallback;
    }

    public GetMissionLootPreviewUseCase(
            MissionDefinitionRepository missionDefinitionRepository,
            ItemDefinitionRepository itemDefinitionRepository
    ) {
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
