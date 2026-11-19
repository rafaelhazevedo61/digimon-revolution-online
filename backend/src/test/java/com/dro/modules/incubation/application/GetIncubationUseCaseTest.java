package com.dro.modules.incubation.application;

import com.dro.modules.incubation.api.dto.response.IncubationSlotsResponse;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetIncubationUseCaseTest {
    @Mock
    private IncubationRepository incubationRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Test
    void returnsAllThreeSlotsWithIndependentIncubations() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .unlockedIncubationSlots(2)
                .build();
        Incubation first = incubation(playerId, 1, ItemType.DIGITAMA_FIRE, ItemType.INCUBATOR_COMMON);
        Incubation second = incubation(playerId, 2, ItemType.DIGITAMA_WATER, ItemType.INCUBATOR_RARE);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of(first, second));

        IncubationSlotsResponse response = new GetIncubationUseCase(incubationRepository, playerRepository)
                .execute(JwtTestToken.create(playerId));

        assertEquals(3, response.totalSlots());
        assertEquals(2, response.unlockedSlots());
        assertEquals(3, response.slots().size());
        assertNotNull(response.slots().get(0).incubation());
        assertEquals(ItemType.DIGITAMA_FIRE, response.slots().get(0).incubation().digitamaType());
        assertNotNull(response.slots().get(1).incubation());
        assertEquals(ItemType.DIGITAMA_WATER, response.slots().get(1).incubation().digitamaType());
        assertTrue(response.slots().get(0).unlocked());
        assertTrue(response.slots().get(1).unlocked());
        assertFalse(response.slots().get(2).unlocked());
        assertNull(response.slots().get(2).incubation());
    }

    @Test
    void marksOnlyExpiredIncubationAsReady() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).build();
        Incubation expired = incubation(playerId, 1, ItemType.DIGITAMA_FIRE, ItemType.INCUBATOR_COMMON);
        expired.setFinishAt(LocalDateTime.now().minusSeconds(1));

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of(expired));

        IncubationSlotsResponse response = new GetIncubationUseCase(incubationRepository, playerRepository)
                .execute(JwtTestToken.create(playerId));

        assertEquals(IncubationStatus.READY, response.slots().get(0).incubation().status());
        assertEquals(0, response.slots().get(0).incubation().remainingSeconds());
    }

    private Incubation incubation(UUID playerId, int slotNumber, ItemType digitamaType, ItemType incubatorType) {
        return Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .slotNumber(slotNumber)
                .digitamaType(digitamaType)
                .incubatorType(incubatorType)
                .status(IncubationStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(1))
                .finishAt(LocalDateTime.now().plusMinutes(4))
                .build();
    }
}
