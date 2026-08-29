package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToggleDigimonLockUseCaseTest {
    @Mock
    private DigimonRepository digimonRepository;

    @Test
    void togglesLockForStoredDigimon() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = digimon(digimonId, playerId, DigimonStatus.STORED);
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));
        when(digimonRepository.save(digimon)).thenReturn(digimon);

        ToggleDigimonLockUseCase useCase = new ToggleDigimonLockUseCase(digimonRepository);

        useCase.execute(JwtTestToken.create(playerId), digimonId);
        assertTrue(digimon.isLocked());
        useCase.execute(JwtTestToken.create(playerId), digimonId);
        assertFalse(digimon.isLocked());
        verify(digimonRepository, org.mockito.Mockito.times(2)).save(digimon);
    }

    @Test
    void rejectsLockingDigimonThatIsNotStored() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = digimon(digimonId, playerId, DigimonStatus.ACTIVE);
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));

        ToggleDigimonLockUseCase useCase = new ToggleDigimonLockUseCase(digimonRepository);

        assertThrows(BadRequestException.class, () -> useCase.execute(JwtTestToken.create(playerId), digimonId));
    }

    @Test
    void rejectsLockingDigimonOwnedByAnotherPlayer() {
        UUID playerId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = digimon(digimonId, ownerId, DigimonStatus.STORED);
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));

        ToggleDigimonLockUseCase useCase = new ToggleDigimonLockUseCase(digimonRepository);

        assertThrows(BadRequestException.class, () -> useCase.execute(JwtTestToken.create(playerId), digimonId));
    }

    private Digimon digimon(UUID id, UUID playerId, DigimonStatus status) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .status(status)
                .build();
    }
}
