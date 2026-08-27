package com.dro.modules.mission.application;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.domain.LootTableEntryEntity;
import com.dro.modules.loot.domain.LootTableRarityWeightEntity;
import com.dro.modules.mission.api.dto.response.MissionLootPreviewResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionLootChanceEntity;
import com.dro.modules.mission.domain.MissionLootItemEntity;
import com.dro.modules.mission.domain.MissionRewardEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetMissionLootPreviewUseCaseTest {

    @Test
    void returnsFixedRewardsAndLegacyLootWithCatalogNames() {
        String missionId = "MISSION_NF_2";
        MissionDefinitionEntity mission = MissionDefinitionEntity.builder()
                .id(missionId)
                .name("Patrulha na Floresta Nativa")
                .area(Area.NATIVE_FOREST)
                .requiredLevel(3)
                .baseXp(120)
                .baseBits(60)
                .rewards(List.of(
                        MissionRewardEntity.builder()
                                .itemType(ItemType.TRAINING_STONE)
                                .baseQuantity(2)
                                .build()
                ))
                .lootChances(List.of(
                        MissionLootChanceEntity.builder()
                                .rarity(LootRarity.COMMON)
                                .chance(70)
                                .build()
                ))
                .lootItems(List.of(
                        MissionLootItemEntity.builder()
                                .rarity(LootRarity.COMMON)
                                .itemType(ItemType.DATA_CORE)
                                .quantity(1)
                                .build()
                ))
                .build();

        MissionDefinitionRepository missionRepository = mock(MissionDefinitionRepository.class);
        ItemDefinitionRepository itemRepository = mock(ItemDefinitionRepository.class);
        when(missionRepository.findByIdAndActiveTrue(missionId)).thenReturn(Optional.of(mission));
        when(itemRepository.findByCode("TRAINING_STONE")).thenReturn(Optional.of(item("TRAINING_STONE", "Pedra de Treino")));
        when(itemRepository.findByCode("DATA_CORE")).thenReturn(Optional.of(item("DATA_CORE", "Núcleo de Dados")));

        GetMissionLootPreviewUseCase useCase = new GetMissionLootPreviewUseCase(missionRepository, itemRepository);
        MissionLootPreviewResponse result = useCase.execute(makeToken(), missionId);

        assertEquals(missionId, result.missionId());
        assertEquals(120, result.xpReward());
        assertEquals(60, result.bitsReward());
        assertEquals(1, result.fixedRewards().size());
        assertEquals("Pedra de Treino", result.fixedRewards().get(0).itemName());
        assertEquals(2, result.fixedRewards().get(0).quantity());
        assertEquals(List.of(new MissionLootPreviewResponse.LootChance("COMMON", 70)), result.lootChances());
        assertEquals(1, result.lootItems().size());
        assertEquals("Núcleo de Dados", result.lootItems().get(0).itemName());
        assertEquals(1, result.lootItems().get(0).quantity());
        assertEquals(null, result.chest());
    }

    @Test
    void returnsActiveChestContentsAndRelativeWeights() {
        String missionId = "MISSION_IM_3";
        LootTableEntity lootTable = LootTableEntity.builder()
                .code("LOOT_TABLE_MISSION_IM_3")
                .name("Loot Montanha Infinita")
                .minItems(1)
                .maxItems(2)
                .rarityWeights(List.of(
                        LootTableRarityWeightEntity.builder()
                                .rarity(LootRarity.COMMON)
                                .weight(70)
                                .build(),
                        LootTableRarityWeightEntity.builder()
                                .rarity(LootRarity.RARE)
                                .weight(30)
                                .build()
                ))
                .entries(List.of(
                        LootTableEntryEntity.builder()
                                .rarity(LootRarity.COMMON)
                                .itemType(ItemType.TRAINING_STONE)
                                .weight(10)
                                .minQuantity(1)
                                .maxQuantity(3)
                                .active(true)
                                .build(),
                        LootTableEntryEntity.builder()
                                .rarity(LootRarity.RARE)
                                .itemType(ItemType.DATA_CORE)
                                .weight(5)
                                .minQuantity(1)
                                .maxQuantity(1)
                                .active(false)
                                .build()
                ))
                .build();
        ChestDefinitionEntity chest = ChestDefinitionEntity.builder()
                .code("CHEST_MISSION_IM_3")
                .name("Baú da Montanha Infinita")
                .description("Recompensas da montanha")
                .lootTable(lootTable)
                .build();
        MissionDefinitionEntity mission = MissionDefinitionEntity.builder()
                .id(missionId)
                .name("Missão da Montanha")
                .area(Area.INFINITY_MOUNTAIN)
                .requiredLevel(70)
                .chestDefinition(chest)
                .build();

        MissionDefinitionRepository missionRepository = mock(MissionDefinitionRepository.class);
        ItemDefinitionRepository itemRepository = mock(ItemDefinitionRepository.class);
        when(missionRepository.findByIdAndActiveTrue(missionId)).thenReturn(Optional.of(mission));
        when(itemRepository.findByCode("TRAINING_STONE")).thenReturn(Optional.of(item("TRAINING_STONE", "Pedra de Treino")));

        GetMissionLootPreviewUseCase useCase = new GetMissionLootPreviewUseCase(missionRepository, itemRepository);
        MissionLootPreviewResponse result = useCase.execute(makeToken(), missionId);

        assertNotNull(result.chest());
        assertEquals("CHEST_MISSION_IM_3", result.chest().code());
        assertEquals(1, result.chest().minItems());
        assertEquals(2, result.chest().maxItems());
        assertEquals(2, result.chest().rarityWeights().size());
        assertEquals(1, result.chest().items().size());
        assertEquals("TRAINING_STONE", result.chest().items().get(0).itemCode());
        assertEquals("Pedra de Treino", result.chest().items().get(0).itemName());
        assertEquals(1, result.chest().items().get(0).minQuantity());
        assertEquals(3, result.chest().items().get(0).maxQuantity());
    }

    private ItemDefinition item(String code, String name) {
        return ItemDefinition.builder()
                .code(code)
                .name(name)
                .build();
    }

    private String makeToken() {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", UUID.randomUUID().toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
