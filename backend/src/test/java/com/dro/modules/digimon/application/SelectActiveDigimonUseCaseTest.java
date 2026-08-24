package com.dro.modules.digimon.application;
import com.dro.shared.security.JwtTestToken;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
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
class SelectActiveDigimonUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @InjectMocks
    private SelectActiveDigimonUseCase selectActiveDigimonUseCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = JwtTestToken.create(playerId);
    }

    @Test
    void execute_setsActiveDigimon() {
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .hp(10)
                .attack(5)
                .defense(5)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(10)
                .maxEnergy(10)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Player player = Player.builder()
                .id(playerId)
                .username("test")
                .email("test@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .build();

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        selectActiveDigimonUseCase.execute(token, digimonId);

        assertEquals(digimonId, player.getActiveDigimonId());
        verify(playerRepository).save(player);
    }

    @Test
    void execute_throwsWhenDigimonNotFound() {
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> selectActiveDigimonUseCase.execute(token, digimonId));
    }

    @Test
    void execute_throwsWhenDigimonNotBelongsToPlayer() {
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(UUID.randomUUID())
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .hp(10)
                .attack(5)
                .defense(5)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(10)
                .maxEnergy(10)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        assertThrows(RuntimeException.class,
                () -> selectActiveDigimonUseCase.execute(token, digimonId));
    }
}
