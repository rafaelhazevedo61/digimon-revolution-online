package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenameDigimonUseCaseTest {

    @Mock
    private DigimonRepository digimonRepository;

    @InjectMocks
    private RenameDigimonUseCase renameDigimonUseCase;

    @Test
    void execute_validRename_updatesName() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = UUID.randomUUID() + ":" + playerId;

        Digimon digimon = buildDigimon(digimonId, playerId, "Agumon");

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        renameDigimonUseCase.execute(token, digimonId, "WarGreymon");

        assertEquals("WarGreymon", digimon.getName());
        verify(digimonRepository).save(digimon);
    }

    @Test
    void execute_trimsWhitespace() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = UUID.randomUUID() + ":" + playerId;

        Digimon digimon = buildDigimon(digimonId, playerId, "Agumon");

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        renameDigimonUseCase.execute(token, digimonId, "  Omegamon  ");

        assertEquals("Omegamon", digimon.getName());
    }

    @Test
    void execute_digimonNotFound_throwsNotFoundException() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = UUID.randomUUID() + ":" + playerId;

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                renameDigimonUseCase.execute(token, digimonId, "NewName"));
    }

    @Test
    void execute_digimonBelongsToAnotherPlayer_throwsForbiddenException() {
        UUID playerId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = UUID.randomUUID() + ":" + playerId;

        Digimon digimon = buildDigimon(digimonId, otherPlayerId, "Agumon");

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        assertThrows(ForbiddenException.class, () ->
                renameDigimonUseCase.execute(token, digimonId, "NewName"));
    }

    private Digimon buildDigimon(UUID id, UUID playerId, String name) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .name(name)
                .type("FIRE")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .hp(50)
                .attack(25)
                .defense(25)
                .ivHp(50)
                .ivAttack(50)
                .ivDefense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .trait(Trait.BERSERKER)
                .energy(20)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
