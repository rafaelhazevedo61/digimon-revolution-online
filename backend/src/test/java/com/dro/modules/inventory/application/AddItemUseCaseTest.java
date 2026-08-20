package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
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

    @InjectMocks
    private AddItemUseCase addItemUseCase;

    @Test
    void execute_createsNewItem_whenNotExists() {
        UUID digimonId = UUID.randomUUID();

        when(repository.findByDigimonIdAndItemType(digimonId, ItemType.TRAINING_STONE))
                .thenReturn(Optional.empty());

        addItemUseCase.execute(digimonId, ItemType.TRAINING_STONE, 5);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());

        InventoryItem saved = captor.getValue();
        assertEquals(digimonId, saved.getDigimonId());
        assertEquals(ItemType.TRAINING_STONE, saved.getItemType());
        assertEquals(5, saved.getQuantity());
        assertNotNull(saved.getId());
    }

    @Test
    void execute_incrementsQuantity_whenExists() {
        UUID digimonId = UUID.randomUUID();

        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .itemType(ItemType.DATA_CORE)
                .quantity(3)
                .build();

        when(repository.findByDigimonIdAndItemType(digimonId, ItemType.DATA_CORE))
                .thenReturn(Optional.of(existing));

        addItemUseCase.execute(digimonId, ItemType.DATA_CORE, 7);

        assertEquals(10, existing.getQuantity());
        verify(repository).save(existing);
    }

    @Test
    void execute_grantsDigitama() {
        UUID digimonId = UUID.randomUUID();

        when(repository.findByDigimonIdAndItemType(digimonId, ItemType.DIGITAMA_FIRE))
                .thenReturn(Optional.empty());

        addItemUseCase.execute(digimonId, ItemType.DIGITAMA_FIRE, 1);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());

        assertEquals(ItemType.DIGITAMA_FIRE, captor.getValue().getItemType());
        assertEquals(1, captor.getValue().getQuantity());
    }

    @Test
    void addMaterial_resolvesChestDefinitionAsLootChest() {
        UUID digimonId = UUID.randomUUID();
        var chestDefinition = com.dro.modules.inventory.domain.ItemDefinition.builder()
                .id(101L)
                .code("CHEST_MISSION_NATIVE_FOREST")
                .category("CHEST")
                .maxStack(999)
                .build();

        when(repository.findByDigimonIdAndItemDefinitionId(digimonId, 101L))
                .thenReturn(Optional.empty());

        addItemUseCase.addMaterial(digimonId, chestDefinition, 1);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());

        assertEquals(ItemType.LOOT_CHEST, captor.getValue().getItemType());
        assertSame(chestDefinition, captor.getValue().getItemDefinition());
        assertEquals(1, captor.getValue().getQuantity());
    }

    @Test
    void execute_grantsFragment() {
        UUID digimonId = UUID.randomUUID();

        when(repository.findByDigimonIdAndItemType(digimonId, ItemType.FRAGMENT_MEGA))
                .thenReturn(Optional.empty());

        addItemUseCase.execute(digimonId, ItemType.FRAGMENT_MEGA, 10);

        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(repository).save(captor.capture());

        assertEquals(ItemType.FRAGMENT_MEGA, captor.getValue().getItemType());
        assertEquals(10, captor.getValue().getQuantity());
    }
}
