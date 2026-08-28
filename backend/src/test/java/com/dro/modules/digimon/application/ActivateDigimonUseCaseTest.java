package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateDigimonUseCaseTest {
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;


    @Test
    void execute_movesPreviousActiveToStorageBeforeActivatingSelectedDigimon() {
        UUID playerId = UUID.randomUUID();
        UUID previousId = UUID.randomUUID();
        UUID selectedId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(previousId)
                .maxStorageSlots(50)
                .build();
        Digimon previous = digimon(previousId, playerId, DigimonStatus.ACTIVE);
        UUID weaponId = UUID.randomUUID();
        UUID armorId = UUID.randomUUID();
        UUID accessoryId = UUID.randomUUID();
        previous.setEquipmentBySlot(EquipmentSlot.WEAPON, weaponId);
        previous.setEquipmentBySlot(EquipmentSlot.ARMOR, armorId);
        previous.setEquipmentBySlot(EquipmentSlot.ACCESSORY, accessoryId);
        Digimon selected = digimon(selectedId, playerId, DigimonStatus.HATCHED);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(selectedId)).thenReturn(Optional.of(selected));
        when(digimonRepository.findByPlayerIdAndStatusForUpdate(playerId, DigimonStatus.ACTIVE))
                .thenReturn(List.of(previous));
        when(digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED)).thenReturn(0L);

        ActivateDigimonUseCase useCase = new ActivateDigimonUseCase(
                playerRepository, digimonRepository
        );

        Digimon result = useCase.execute(JwtTestToken.create(playerId), selectedId);

        assertEquals(selectedId, result.getId());
        assertEquals(DigimonStatus.STORED, previous.getStatus());
        assertEquals(weaponId, previous.getEquipmentIdBySlot(EquipmentSlot.WEAPON));
        assertEquals(armorId, previous.getEquipmentIdBySlot(EquipmentSlot.ARMOR));
        assertEquals(accessoryId, previous.getEquipmentIdBySlot(EquipmentSlot.ACCESSORY));
        assertEquals(DigimonStatus.ACTIVE, selected.getStatus());
        assertEquals(selectedId, player.getActiveDigimonId());
        verify(digimonRepository).save(previous);
        verify(digimonRepository).flush();
        verify(digimonRepository).save(selected);
        verify(playerRepository).save(player);
    }

    @Test
    void execute_allowsReplacingActiveWhenStorageIsFullBecauseSelectedDigimonFreesItsSlot() {
        UUID playerId = UUID.randomUUID();
        UUID previousId = UUID.randomUUID();
        UUID selectedId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(previousId)
                .maxStorageSlots(1)
                .build();
        Digimon previous = digimon(previousId, playerId, DigimonStatus.ACTIVE);
        Digimon selected = digimon(selectedId, playerId, DigimonStatus.STORED);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(selectedId)).thenReturn(Optional.of(selected));
        when(digimonRepository.findByPlayerIdAndStatusForUpdate(playerId, DigimonStatus.ACTIVE))
                .thenReturn(List.of(previous));
        when(digimonRepository.countByPlayerIdAndStatus(eq(playerId), eq(DigimonStatus.STORED)))
                .thenReturn(1L);

        ActivateDigimonUseCase useCase = new ActivateDigimonUseCase(
                playerRepository, digimonRepository
        );

        useCase.executeStored(JwtTestToken.create(playerId), selectedId);

        assertEquals(DigimonStatus.STORED, previous.getStatus());
        assertEquals(DigimonStatus.ACTIVE, selected.getStatus());
        assertEquals(selectedId, player.getActiveDigimonId());
    }

    private Digimon digimon(UUID id, UUID playerId, DigimonStatus status) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .status(status)
                .build();
    }
}
