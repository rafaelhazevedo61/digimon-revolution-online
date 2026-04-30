package com.dro.modules.digimon.application;

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
class AddExperienceUseCaseTest {

    @Mock
    private DigimonRepository repository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private AddExperienceUseCase addExperienceUseCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Player player;
    private Digimon digimon;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = UUID.randomUUID() + ":" + playerId;

        player = Player.builder()
                .id(playerId)
                .username("test")
                .email("test@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .build();

        digimon = Digimon.builder()
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
                .trait(null)
                .energy(10)
                .maxEnergy(10)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .rebirthCount(0)
                .bits(0)
                .build();
    }

    @Test
    void execute_addsExperienceAndSaves() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(repository.findById(digimonId)).thenReturn(Optional.of(digimon));

        addExperienceUseCase.execute(token, 50);

        assertEquals(50, digimon.getExperience());
        verify(repository).save(digimon);
    }

    @Test
    void execute_levelsUpWithEnoughXp() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(repository.findById(digimonId)).thenReturn(Optional.of(digimon));

        addExperienceUseCase.execute(token, 100);

        assertEquals(2, digimon.getLevel());
        verify(repository).save(digimon);
    }

    @Test
    void execute_throwsWhenNoActiveDigimon() {
        player.setActiveDigimonId(null);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(RuntimeException.class,
                () -> addExperienceUseCase.execute(token, 100));
    }

    @Test
    void execute_throwsWhenPlayerNotFound() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> addExperienceUseCase.execute(token, 100));
    }
}
