package com.dro.modules.digitama.application;

import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelectDigitamaUseCaseTest {

    @Test
    void rejectsSelectionWhenPoolHasNoEligibleBaby() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        DigitamaPoolRepository poolRepository = mock(DigitamaPoolRepository.class);
        DigitamaPoolEligibilityService eligibilityService = mock(DigitamaPoolEligibilityService.class);
        TutorialService tutorialService = mock(TutorialService.class);
        UUID playerId = UUID.randomUUID();
        DigitamaPool steelPool = DigitamaPool.builder()
                .code("DIGITAMA_STEEL")
                .entries(List.of())
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(Player.builder().id(playerId).build()));
        when(poolRepository.findByCodeAndActiveTrueAndContentActiveTrue("DIGITAMA_STEEL"))
                .thenReturn(Optional.of(steelPool));
        when(eligibilityService.getEligibleEntries(steelPool)).thenReturn(List.of());

        SelectDigitamaUseCase useCase = new SelectDigitamaUseCase(
                playerRepository,
                poolRepository,
                eligibilityService,
                tutorialService
        );

        assertThrows(BadRequestException.class, () -> useCase.execute(JwtTestToken.create(playerId), DigitamaType.STEEL));
        verify(playerRepository, never()).save(org.mockito.ArgumentMatchers.any(Player.class));
    }
}
