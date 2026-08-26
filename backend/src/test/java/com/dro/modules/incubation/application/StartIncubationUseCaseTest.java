package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartIncubationUseCaseTest {
    @Mock
    private IncubationRepository incubationRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Test
    void startsIncubationInUnlockedSlot() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(digimonId)
                .unlockedIncubationSlots(2)
                .build();
        InventoryItem digitama = InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .itemType(ItemType.DIGITAMA_FIRE)
                .quantity(2)
                .build();
        InventoryItem incubator = InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .itemType(ItemType.INCUBATOR_COMMON)
                .quantity(2)
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(incubationRepository.findByPlayerIdAndSlotNumberAndStatusNot(playerId, 2, IncubationStatus.CLAIMED))
                .thenReturn(Optional.empty());
        when(inventoryRepository.findByDigimonIdAndItemType(digimonId, ItemType.DIGITAMA_FIRE))
                .thenReturn(Optional.of(digitama));
        when(inventoryRepository.findByDigimonIdAndItemType(digimonId, ItemType.INCUBATOR_COMMON))
                .thenReturn(Optional.of(incubator));

        new StartIncubationUseCase(incubationRepository, inventoryRepository, playerRepository)
                .execute(JwtTestToken.create(playerId), 2, ItemType.DIGITAMA_FIRE, ItemType.INCUBATOR_COMMON);

        ArgumentCaptor<com.dro.modules.incubation.domain.Incubation> captor =
                ArgumentCaptor.forClass(com.dro.modules.incubation.domain.Incubation.class);
        verify(incubationRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getSlotNumber());
        assertEquals(1, digitama.getQuantity());
        assertEquals(1, incubator.getQuantity());
    }

    @Test
    void rejectsLockedSlotBeforeConsumingItems() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(UUID.randomUUID())
                .unlockedIncubationSlots(1)
                .build();
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));

        assertThrows(
                ConflictException.class,
                () -> new StartIncubationUseCase(incubationRepository, inventoryRepository, playerRepository)
                        .execute(JwtTestToken.create(playerId), 2, ItemType.DIGITAMA_FIRE, ItemType.INCUBATOR_COMMON)
        );

        verify(inventoryRepository, never()).save(any());
        verify(incubationRepository, never()).save(any());
    }
}
