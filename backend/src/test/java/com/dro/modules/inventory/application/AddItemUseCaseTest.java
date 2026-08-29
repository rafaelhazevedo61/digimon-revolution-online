package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddItemUseCaseTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private ItemDefinitionRepository itemDefinitionRepository;

    @InjectMocks
    private AddItemUseCase addItemUseCase;

    @Test
    void execute_createsNewItemForPlayer_whenNotExists() {
        UUID playerId = UUID.randomUUID();
        when(itemDefinitionRepository.findByCode("TRAINING_STONE")).thenReturn(Optional.empty());
        when(repository.findByPlayerIdAndItemType(playerId, ItemType.TRAINING_STONE)).thenReturn(Optional.empty());

        addItemUseCase.execute(playerId, ItemType.TRAINING_STONE, 5);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());
        InventoryItem saved = captor.getValue();
        assertEquals(playerId, saved.getPlayerId());
        assertEquals(ItemType.TRAINING_STONE, saved.getItemType());
        assertEquals(5, saved.getQuantity());
        assertNotNull(saved.getId());
    }

    @Test
    void execute_incrementsQuantity_whenExists() {
        UUID playerId = UUID.randomUUID();
        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .itemType(ItemType.DATA_CORE)
                .quantity(3)
                .build();
        when(itemDefinitionRepository.findByCode("DATA_CORE")).thenReturn(Optional.empty());
        when(repository.findByPlayerIdAndItemType(playerId, ItemType.DATA_CORE)).thenReturn(Optional.of(existing));

        addItemUseCase.execute(playerId, ItemType.DATA_CORE, 7);

        assertEquals(10, existing.getQuantity());
        verify(repository).save(existing);
    }

    @Test
    void executeUsesCatalogDefinitionWhenAvailable() {
        UUID playerId = UUID.randomUUID();
        ItemDefinition definition = ItemDefinition.builder()
                .id(202L).code("TRAINING_STONE").name("Pedra de Treino")
                .category("MATERIAL").maxStack(999).build();
        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID()).playerId(playerId).itemType(ItemType.TRAINING_STONE)
                .itemDefinition(definition).quantity(9).build();
        when(itemDefinitionRepository.findByCode("TRAINING_STONE")).thenReturn(Optional.of(definition));
        when(repository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, 202L)).thenReturn(Optional.of(existing));

        addItemUseCase.execute(playerId, ItemType.TRAINING_STONE, 2);

        assertEquals(11, existing.getQuantity());
        verify(repository).save(existing);
    }

    @Test
    void execute_discardsAmountAboveMaxStack() {
        UUID playerId = UUID.randomUUID();
        ItemDefinition definition = ItemDefinition.builder()
                .id(203L).code("TRAINING_STONE").category("MATERIAL").maxStack(10).build();
        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID()).playerId(playerId).itemDefinition(definition)
                .itemType(ItemType.TRAINING_STONE).quantity(8).build();
        when(itemDefinitionRepository.findByCode("TRAINING_STONE")).thenReturn(Optional.of(definition));
        when(repository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, 203L)).thenReturn(Optional.of(existing));

        addItemUseCase.execute(playerId, ItemType.TRAINING_STONE, 5);

        assertEquals(10, existing.getQuantity());
        verify(repository).save(existing);
    }

    @Test
    void addMaterial_resolvesChestDefinitionAsLootChest() {
        UUID playerId = UUID.randomUUID();
        ItemDefinition chestDefinition = ItemDefinition.builder()
                .id(101L).code("CHEST_MISSION_NATIVE_FOREST").category("CHEST").maxStack(999).build();
        when(repository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, 101L)).thenReturn(Optional.empty());

        addItemUseCase.addMaterial(playerId, chestDefinition, 1);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());
        assertEquals(playerId, captor.getValue().getPlayerId());
        assertEquals(ItemType.LOOT_CHEST, captor.getValue().getItemType());
        assertSame(chestDefinition, captor.getValue().getItemDefinition());
        assertEquals(1, captor.getValue().getQuantity());
    }
}
