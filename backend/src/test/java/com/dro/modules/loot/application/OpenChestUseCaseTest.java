package com.dro.modules.loot.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.api.dto.request.OpenChestRequest;
import com.dro.modules.loot.api.dto.response.ChestOpeningResponse;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.ChestLootRoller;
import com.dro.modules.loot.domain.ChestOpeningEntity;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.domain.LootTableEntryEntity;
import com.dro.modules.loot.domain.LootTableRarityWeightEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.loot.infra.ChestOpeningRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenChestUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemDefinitionRepository itemDefinitionRepository;

    @Mock
    private ChestDefinitionRepository chestDefinitionRepository;

    @Mock
    private ChestOpeningRepository chestOpeningRepository;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    @Mock
    private ChestLootRoller chestLootRoller;

    @InjectMocks
    private OpenChestUseCase openChestUseCase;

    @Test
    void executeConsumesOneChestCreditsRewardPersistsOpeningAndAudits() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String chestCode = "CHEST_MISSION_NATIVE_FOREST";
        ItemDefinition chestDefinition = itemDefinition(1L, chestCode, "Baú Floresta Nativa", "CHEST", 99);
        ItemDefinition rewardDefinition = itemDefinition(2L, "FRAGMENT_AGUMON", "Fragmento do Agumon", "EVOLUTION_MATERIAL", 999);
        ChestDefinitionEntity chest = chest(chestCode, chestDefinition, rewardDefinition);
        Player player = Player.builder().id(playerId).activeDigimonId(digimonId).build();
        Digimon digimon = Digimon.builder().id(digimonId).playerId(playerId).build();
        InventoryItem chestInventory = inventory(digimonId, ItemType.LOOT_CHEST, chestDefinition, 2);

        stubCommon(playerId, digimonId, player, digimon, chest, chestInventory, rewardDefinition);
        when(chestOpeningRepository.saveAndFlush(any(ChestOpeningEntity.class)))
                .thenAnswer(invocation -> {
                    ChestOpeningEntity opening = invocation.getArgument(0);
                    opening.setId(10L);
                    return opening;
                });

        ChestOpeningResponse response = openChestUseCase.execute(
                token(playerId),
                new OpenChestRequest(chestCode, "request-1")
        );

        assertThat(response.requestId()).isEqualTo("request-1");
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.replayed()).isFalse();
        assertThat(response.message()).isEqualTo("Baú aberto com sucesso!");
        assertThat(response.chestCode()).isEqualTo(chestCode);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).itemCode()).isEqualTo("FRAGMENT_AGUMON");
        assertThat(response.items().get(0).itemName()).isEqualTo("Fragmento do Agumon");
        assertThat(response.items().get(0).rarity()).isEqualTo(LootRarity.COMMON);
        assertThat(response.items().get(0).quantity()).isBetween(1, 5);
        assertThat(chestInventory.getQuantity()).isEqualTo(1);

        verify(inventoryRepository, times(2)).save(any(InventoryItem.class));
        verify(chestOpeningRepository).saveAndFlush(any(ChestOpeningEntity.class));
        verify(transactionAuditPublisher).success(
                eq("chest-opening:10"),
                eq("CHEST_OPENED"),
                eq("ChestOpening"),
                eq("10"),
                any()
        );
    }

    @Test
    void executeOpensMultipleChestsInOneTransactionAndAggregatesRewards() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String chestCode = "CHEST_MISSION_NATIVE_FOREST";
        ItemDefinition chestDefinition = itemDefinition(1L, chestCode, "Baú Floresta Nativa", "CHEST", 99);
        ItemDefinition rewardDefinition = itemDefinition(2L, "FRAGMENT_AGUMON", "Fragmento do Agumon", "EVOLUTION_MATERIAL", 999);
        ChestDefinitionEntity chest = chest(chestCode, chestDefinition, rewardDefinition);
        Player player = Player.builder().id(playerId).activeDigimonId(digimonId).build();
        Digimon digimon = Digimon.builder().id(digimonId).playerId(playerId).build();
        InventoryItem chestInventory = inventory(digimonId, ItemType.LOOT_CHEST, chestDefinition, 5);

        stubCommon(playerId, digimonId, player, digimon, chest, chestInventory, rewardDefinition);
        when(chestOpeningRepository.saveAndFlush(any(ChestOpeningEntity.class)))
                .thenAnswer(invocation -> {
                    ChestOpeningEntity opening = invocation.getArgument(0);
                    opening.setId(11L);
                    return opening;
                });

        ChestOpeningResponse response = openChestUseCase.execute(
                token(playerId),
                new OpenChestRequest(chestCode, "request-batch-1", 3)
        );

        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.replayed()).isFalse();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).itemCode()).isEqualTo("FRAGMENT_AGUMON");
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(chestInventory.getQuantity()).isEqualTo(2);
        verify(chestLootRoller, times(3)).roll(chest.getLootTable());
        verify(chestOpeningRepository).saveAndFlush(any(ChestOpeningEntity.class));
    }

    @Test
    void executeReturnsPreviousOpeningWithoutConsumingAgainOnRetry() {
        UUID playerId = UUID.randomUUID();
        String chestCode = "CHEST_MISSION_NATIVE_FOREST";
        ItemDefinition chestItem = itemDefinition(1L, chestCode, "Baú Floresta Nativa", "CHEST", 99);
        ChestDefinitionEntity chest = chest(chestCode, chestItem, null);
        ChestOpeningEntity previous = ChestOpeningEntity.builder()
                .id(10L)
                .requestId("request-1")
                .playerId(playerId)
                .chestDefinition(chest)
                .quantity(3)
                .rarity(LootRarity.COMMON)
                .source("PLAYER_INVENTORY")
                .build();
        when(chestOpeningRepository.findByRequestId("request-1")).thenReturn(Optional.of(previous));

        ChestOpeningResponse response = openChestUseCase.execute(
                token(playerId),
                new OpenChestRequest(chestCode, "request-1")
        );

        assertThat(response.requestId()).isEqualTo("request-1");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.replayed()).isTrue();
        assertThat(response.message()).isEqualTo(
                "Esta abertura já havia sido processada. O resultado original foi retornado."
        );
        assertThat(response.rarity()).isEqualTo(LootRarity.COMMON);
        verifyNoInteractions(playerRepository, digimonRepository, inventoryRepository,
                itemDefinitionRepository, chestDefinitionRepository, transactionAuditPublisher);
        verify(chestOpeningRepository, never()).saveAndFlush(any());
    }

    @Test
    void executeRejectsReusingRequestIdForAnotherPlayer() {
        UUID originalPlayerId = UUID.randomUUID();
        UUID differentPlayerId = UUID.randomUUID();
        String chestCode = "CHEST_MISSION_NATIVE_FOREST";
        ItemDefinition chestItem = itemDefinition(1L, chestCode, "Baú Floresta Nativa", "CHEST", 99);
        ChestDefinitionEntity chest = chest(chestCode, chestItem, null);
        ChestOpeningEntity previous = ChestOpeningEntity.builder()
                .id(10L)
                .requestId("request-1")
                .playerId(originalPlayerId)
                .chestDefinition(chest)
                .rarity(LootRarity.COMMON)
                .source("PLAYER_INVENTORY")
                .build();
        when(chestOpeningRepository.findByRequestId("request-1")).thenReturn(Optional.of(previous));

        assertThatThrownBy(() -> openChestUseCase.execute(
                token(differentPlayerId),
                new OpenChestRequest(chestCode, "request-1")
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("already associated");
    }

    @Test
    void executeDoesNotConsumeChestWhenRewardCannotFitTheStack() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String chestCode = "CHEST_MISSION_NATIVE_FOREST";
        ItemDefinition chestItem = itemDefinition(1L, chestCode, "Baú Floresta Nativa", "CHEST", 99);
        ItemDefinition rewardDefinition = itemDefinition(2L, "FRAGMENT_AGUMON", "Fragmento do Agumon", "EVOLUTION_MATERIAL", 2);
        ChestDefinitionEntity chest = chest(chestCode, chestItem, rewardDefinition);
        Player player = Player.builder().id(playerId).activeDigimonId(digimonId).build();
        Digimon digimon = Digimon.builder().id(digimonId).playerId(playerId).build();
        InventoryItem chestInventory = inventory(digimonId, ItemType.LOOT_CHEST, chestItem, 2);
        InventoryItem existingReward = inventory(digimonId, ItemType.EVOLUTION_MATERIAL, rewardDefinition, 1);

        stubCommon(playerId, digimonId, player, digimon, chest, chestInventory, rewardDefinition);
        when(inventoryRepository.findByDigimonIdAndItemDefinitionIdForUpdate(digimonId, 2L))
                .thenReturn(Optional.of(existingReward));
        when(chestLootRoller.roll(chest.getLootTable())).thenReturn(
                new ChestLootRoller.ChestLootRoll(
                        LootRarity.COMMON,
                        List.of(new ChestLootRoller.ChestLootItem(
                                LootRarity.COMMON,
                                ItemType.EVOLUTION_MATERIAL,
                                "FRAGMENT_AGUMON",
                                3
                        ))
                )
        );

        assertThatThrownBy(() -> openChestUseCase.execute(
                token(playerId),
                new OpenChestRequest(chestCode, "request-1")
        )).isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("max stack");

        assertThat(chestInventory.getQuantity()).isEqualTo(2);
        verify(inventoryRepository, never()).delete(chestInventory);
        verify(chestOpeningRepository, never()).saveAndFlush(any());
        verifyNoInteractions(transactionAuditPublisher);
    }

    private void stubCommon(
            UUID playerId,
            UUID digimonId,
            Player player,
            Digimon digimon,
            ChestDefinitionEntity chest,
            InventoryItem chestInventory,
            ItemDefinition rewardDefinition
    ) {
        when(chestOpeningRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));
        when(chestDefinitionRepository.findWithCatalogByCode(chest.getCode())).thenReturn(Optional.of(chest));
        when(inventoryRepository.findByDigimonIdAndItemDefinitionIdForUpdate(digimonId, 1L))
                .thenReturn(Optional.of(chestInventory));
        when(inventoryRepository.findByDigimonIdAndItemDefinitionIdForUpdate(digimonId, 2L))
                .thenReturn(Optional.empty());
        when(itemDefinitionRepository.findByCode("FRAGMENT_AGUMON"))
                .thenReturn(Optional.of(rewardDefinition));
        when(chestLootRoller.roll(chest.getLootTable())).thenReturn(
                new ChestLootRoller.ChestLootRoll(
                        LootRarity.COMMON,
                        List.of(new ChestLootRoller.ChestLootItem(
                                LootRarity.COMMON,
                                ItemType.EVOLUTION_MATERIAL,
                                "FRAGMENT_AGUMON",
                                1
                        ))
                )
        );
    }

    private String token(UUID playerId) {
        return JwtTokenCodec.create(
                java.util.Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
    }

    private ChestDefinitionEntity chest(
            String code,
            ItemDefinition chestDefinition,
            ItemDefinition rewardDefinition
    ) {
        LootTableEntity table = LootTableEntity.builder()
                .active(true)
                .minItems(1)
                .maxItems(1)
                .build();
        for (LootRarity rarity : LootRarity.values()) {
            table.getRarityWeights().add(LootTableRarityWeightEntity.builder()
                    .lootTable(table)
                    .rarity(rarity)
                    .weight(rarity == LootRarity.COMMON ? 100 : 1)
                    .build());
            table.getEntries().add(LootTableEntryEntity.builder()
                    .lootTable(table)
                    .rarity(rarity)
                    .itemType(ItemType.EVOLUTION_MATERIAL)
                    .materialCode("FRAGMENT_AGUMON")
                    .weight(1)
                    .minQuantity(rewardDefinition == null ? 1 : 1)
                    .maxQuantity(5)
                    .active(true)
                    .build());
        }
        return ChestDefinitionEntity.builder()
                .id(1L)
                .code(code)
                .name(chestDefinition.getName())
                .lootTable(table)
                .itemDefinition(chestDefinition)
                .tradable(true)
                .active(true)
                .build();
    }

    private ItemDefinition itemDefinition(
            Long id,
            String code,
            String name,
            String category,
            int maxStack
    ) {
        return ItemDefinition.builder()
                .id(id)
                .code(code)
                .name(name)
                .category(category)
                .maxStack(maxStack)
                .build();
    }

    private InventoryItem inventory(
            UUID digimonId,
            ItemType itemType,
            ItemDefinition definition,
            int quantity
    ) {
        return InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .itemType(itemType)
                .itemDefinition(definition)
                .quantity(quantity)
                .build();
    }
}
