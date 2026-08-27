package com.dro.modules.digimon.application;

import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SelectActiveDigimonUseCaseTest {
    @Mock
    private ActivateDigimonUseCase activateDigimonUseCase;

    @Test
    void execute_delegatesToSingleActiveUseCase() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = JwtTestToken.create(playerId);
        SelectActiveDigimonUseCase useCase = new SelectActiveDigimonUseCase(activateDigimonUseCase);

        useCase.execute(token, digimonId);

        verify(activateDigimonUseCase).execute(token, digimonId);
    }
}
