package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SacrificeDigimonUseCaseTest {
    @Mock
    private DigimonRepository digimonRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MissionInstanceRepository missionInstanceRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @Test
    void rejectsLockedDigimonWithoutChangingAnyRecord() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).digitalData(10).build();
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .status(DigimonStatus.STORED)
                .locked(true)
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));

        SacrificeDigimonUseCase useCase = new SacrificeDigimonUseCase(
                digimonRepository, playerRepository, missionInstanceRepository, inventoryRepository
        );

        assertThrows(BadRequestException.class, () -> useCase.execute(
                JwtTestToken.create(playerId), digimonId));

        assertEquals(10, player.getDigitalData());
        assertEquals(DigimonStatus.STORED, digimon.getStatus());
        verify(digimonRepository, never()).save(any());
        verify(playerRepository, never()).save(any());
    }
}
