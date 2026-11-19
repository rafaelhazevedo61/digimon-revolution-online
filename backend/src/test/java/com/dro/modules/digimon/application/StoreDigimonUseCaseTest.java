package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.infra.EquipmentRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreDigimonUseCaseTest {
    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Test
    void execute_storesNewlyHatchedDigimon() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).maxStorageSlots(1).build();
        Digimon digimon = digimon(digimonId, playerId, DigimonStatus.HATCHED);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));
        when(digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED)).thenReturn(0L);

        StoreDigimonUseCase useCase = new StoreDigimonUseCase(
                digimonRepository, playerRepository, equipmentRepository
        );

        useCase.execute(JwtTestToken.create(playerId), digimonId);

        assertEquals(DigimonStatus.STORED, digimon.getStatus());
        verify(digimonRepository).save(digimon);
    }

    @Test
    void execute_rejectsNewlyHatchedDigimonWhenStorageIsFull() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).maxStorageSlots(1).build();
        Digimon digimon = digimon(digimonId, playerId, DigimonStatus.HATCHED);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));
        when(digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED)).thenReturn(1L);

        StoreDigimonUseCase useCase = new StoreDigimonUseCase(
                digimonRepository, playerRepository, equipmentRepository
        );

        assertThrows(BadRequestException.class,
                () -> useCase.execute(JwtTestToken.create(playerId), digimonId));
        assertEquals(DigimonStatus.HATCHED, digimon.getStatus());
    }

    private Digimon digimon(UUID id, UUID playerId, DigimonStatus status) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .status(status)
                .build();
    }
}
