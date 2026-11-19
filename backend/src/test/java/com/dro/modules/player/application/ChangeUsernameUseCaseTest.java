package com.dro.modules.player.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeUsernameUseCaseTest {
    @Test
    void calculateCost_usesConfiguredIncrementalSequence() {
        var useCase = new ChangeUsernameUseCase(null, null, null, "1000,5000,10000");

        assertEquals(1000, useCase.calculateCost(0));
        assertEquals(5000, useCase.calculateCost(1));
        assertEquals(10000, useCase.calculateCost(2));
        assertEquals(20000, useCase.calculateCost(3));
        assertEquals(30000, useCase.calculateCost(4));
    }

    @Test
    void constructor_rejectsNonIncreasingCosts() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChangeUsernameUseCase(null, null, null, "1000,1000,5000")
        );
    }
}
