package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.BulkSacrificeDigimonResponse;
import com.dro.modules.digimon.domain.DigitalDataRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkSacrificeDigimonUseCaseTest {
    @Mock
    private DigimonRepository digimonRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MissionInstanceRepository missionInstanceRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @Test
    void sacrificesSelectedStoredDigimonsAndAggregatesDigitalData() {
        UUID playerId = UUID.randomUUID();
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).digitalData(10).build();
        Digimon first = digimon(firstId, playerId, 3);
        Digimon second = digimon(secondId, playerId, 7);
        int expectedFirst = DigitalDataRules.calculate(first.getStage(), first.getLevel(), first.getIvHp(), first.getIvAttack(), first.getIvDefense());
        int expectedSecond = DigitalDataRules.calculate(second.getStage(), second.getLevel(), second.getIvHp(), second.getIvAttack(), second.getIvDefense());

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findAllByIdForUpdate(playerId, List.of(firstId, secondId)))
                .thenReturn(List.of(first, second));
        when(missionInstanceRepository.existsByDigimonIdAndStatus(any(), any())).thenReturn(false);

        BulkSacrificeDigimonResponse result = new BulkSacrificeDigimonUseCase(
                digimonRepository, playerRepository, missionInstanceRepository, inventoryRepository
        ).execute(JwtTestToken.create(playerId), List.of(firstId, secondId));

        assertEquals(2, result.sacrificedCount());
        assertEquals(expectedFirst + expectedSecond, result.digitalDataReceived());
        assertEquals(10 + expectedFirst + expectedSecond, player.getDigitalData());
        assertEquals(DigimonStatus.SACRIFICED, first.getStatus());
        assertEquals(DigimonStatus.SACRIFICED, second.getStatus());
        verify(inventoryRepository).deleteByDigimonId(firstId);
        verify(inventoryRepository).deleteByDigimonId(secondId);
        verify(playerRepository).save(player);
    }

    @Test
    void rejectsLockedDigimonWithoutChangingAnyRecord() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).digitalData(10).build();
        Digimon locked = digimon(digimonId, playerId, 3);
        locked.setLocked(true);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findAllByIdForUpdate(playerId, List.of(digimonId)))
                .thenReturn(List.of(locked));

        BulkSacrificeDigimonUseCase useCase = new BulkSacrificeDigimonUseCase(
                digimonRepository, playerRepository, missionInstanceRepository, inventoryRepository
        );

        assertThrows(BadRequestException.class, () -> useCase.execute(
                JwtTestToken.create(playerId), List.of(digimonId)));

        assertEquals(10, player.getDigitalData());
        assertEquals(DigimonStatus.STORED, locked.getStatus());
        verify(digimonRepository, never()).save(any());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateDigimonIdsBeforeLoadingRecords() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).build();
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));

        BulkSacrificeDigimonUseCase useCase = new BulkSacrificeDigimonUseCase(
                digimonRepository, playerRepository, missionInstanceRepository, inventoryRepository
        );

        assertThrows(RuntimeException.class, () -> useCase.execute(
                JwtTestToken.create(playerId), List.of(digimonId, digimonId)));
    }

    @Test
    void processesMoreThanOneHundredDigimonsInBatches() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).digitalData(10).build();
        List<UUID> ids = new ArrayList<>();
        List<Digimon> digimons = new ArrayList<>();
        for (int index = 0; index < 201; index++) {
            UUID digimonId = UUID.randomUUID();
            ids.add(digimonId);
            digimons.add(digimon(digimonId, playerId, 3));
        }

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findAllByIdForUpdate(eq(playerId), anyList())).thenAnswer(invocation -> {
            List<UUID> batchIds = invocation.getArgument(1);
            return digimons.stream().filter(digimon -> batchIds.contains(digimon.getId())).toList();
        });
        when(missionInstanceRepository.existsByDigimonIdAndStatus(any(), any())).thenReturn(false);

        BulkSacrificeDigimonResponse result = new BulkSacrificeDigimonUseCase(
                digimonRepository, playerRepository, missionInstanceRepository, inventoryRepository
        ).execute(JwtTestToken.create(playerId), ids);

        assertEquals(201, result.sacrificedCount());
        verify(digimonRepository, times(3)).findAllByIdForUpdate(eq(playerId), anyList());
        verify(inventoryRepository, times(201)).deleteByDigimonId(any());
        verify(digimonRepository, times(201)).save(any());
        verify(playerRepository).save(player);
    }

    private Digimon digimon(UUID id, UUID playerId, int level) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .status(DigimonStatus.STORED)
                .stage(Stage.ROOKIE)
                .level(level)
                .build();
    }
}
