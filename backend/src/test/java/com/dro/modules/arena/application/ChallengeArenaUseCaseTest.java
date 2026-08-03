package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaMatchResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeArenaUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private ArenaMatchRepository arenaMatchRepository;

    @Mock
    private DigimonPowerService digimonPowerService;

    @InjectMocks
    private ChallengeArenaUseCase useCase;

    private UUID playerId;
    private UUID opponentPlayerId;
    private UUID attackerId;
    private UUID defenderId;
    private String token;
    private Player player;
    private Digimon attacker;
    private Digimon defender;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        opponentPlayerId = UUID.randomUUID();
        attackerId = UUID.randomUUID();
        defenderId = UUID.randomUUID();
        token = createToken(playerId);

        player = Player.builder()
                .id(playerId)
                .username("attacker")
                .email("attacker@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(attackerId)
                .userType(UserType.PLAYER)
                .build();

        attacker = digimon(attackerId, playerId, 1000, Stage.ROOKIE, false);
        defender = digimon(defenderId, opponentPlayerId, 1000, Stage.ROOKIE, false);
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private static Digimon digimon(UUID id, UUID owner, int rating, Stage stage, boolean bot) {
        return Digimon.builder()
                .id(id)
                .playerId(owner)
                .name("Agumon")
                .type("FIRE")
                .stage(stage)
                .level(10)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(100)
                .maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .arenaRating(rating)
                .arenaWins(0)
                .arenaLosses(0)
                .bot(bot)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void stubDigimons() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(attackerId)).thenReturn(Optional.of(attacker));
        when(digimonRepository.findById(defenderId)).thenReturn(Optional.of(defender));
    }

    private void stubPowers(double attackerPower, double defenderPower) {
        when(digimonPowerService.calculatePower(attacker)).thenReturn(attackerPower);
        when(digimonPowerService.calculatePower(defender)).thenReturn(defenderPower);
    }

    private ArenaMatchResponse challengeWithRoll(int roll) {
        try (MockedStatic<ThreadLocalRandom> mocked = mockStatic(ThreadLocalRandom.class)) {
            ThreadLocalRandom random = mock(ThreadLocalRandom.class);
            when(random.nextInt(anyInt(), anyInt())).thenReturn(roll);
            mocked.when(ThreadLocalRandom::current).thenReturn(random);
            return useCase.execute(token, defenderId);
        }
    }

    @Test
    void victoryUpdatesAttackerRatingWinsAndGrantsBits() {
        stubDigimons();
        stubPowers(1000.0, 100.0); // atacante muito mais forte -> winChance alta

        ArenaMatchResponse response = challengeWithRoll(1); // roll baixo -> vitória

        assertTrue(response.victory());
        assertEquals(1, attacker.getArenaWins());
        assertEquals(0, attacker.getArenaLosses());
        assertTrue(attacker.getArenaRating() > 1000);
        assertEquals(ArenaRules.winBits(1000, 1000), attacker.getBits());
        assertEquals(ArenaRules.winBits(1000, 1000), response.bitsGained());
        verify(digimonRepository).save(attacker);
        verify(arenaMatchRepository).save(any(ArenaMatch.class));
    }

    @Test
    void defeatUpdatesAttackerRatingLossesAndGrantsNoBits() {
        stubDigimons();
        stubPowers(1000.0, 100.0);

        ArenaMatchResponse response = challengeWithRoll(100); // roll alto -> derrota

        assertFalse(response.victory());
        assertEquals(0, attacker.getArenaWins());
        assertEquals(1, attacker.getArenaLosses());
        assertTrue(attacker.getArenaRating() < 1000);
        assertEquals(0, attacker.getBits());
        assertEquals(0, response.bitsGained());
    }

    @Test
    void realDefenderRatingAndStatsAreUpdatedAndPersisted() {
        stubDigimons();
        stubPowers(1000.0, 100.0);

        challengeWithRoll(1); // vitória do atacante

        assertTrue(defender.getArenaRating() < 1000);
        assertEquals(1, defender.getArenaLosses());
        assertEquals(0, defender.getArenaWins());
        verify(digimonRepository).save(defender);
    }

    @Test
    void botDefenderIsNeitherUpdatedNorPersisted() {
        defender = digimon(defenderId, opponentPlayerId, 1000, Stage.ROOKIE, true);
        stubDigimons();
        stubPowers(1000.0, 100.0);

        challengeWithRoll(1);

        assertEquals(1000, defender.getArenaRating());
        assertEquals(0, defender.getArenaLosses());
        verify(digimonRepository, never()).save(defender);
        verify(digimonRepository).save(attacker);
    }

    @Test
    void nonAdminConsumesEnergy() {
        stubDigimons();
        stubPowers(1000.0, 100.0);

        challengeWithRoll(1);

        assertEquals(100 - ArenaRules.ENERGY_COST, attacker.getEnergy());
    }

    @Test
    void adminDoesNotConsumeEnergy() {
        player.setUserType(UserType.ADMIN);
        stubDigimons();
        stubPowers(1000.0, 100.0);

        challengeWithRoll(1);

        assertEquals(100, attacker.getEnergy());
    }

    @Test
    void throwsWhenChallengingOwnDigimon() {
        defender = digimon(defenderId, playerId, 1000, Stage.ROOKIE, false);
        stubDigimons();

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
        verify(arenaMatchRepository, never()).save(any());
    }

    @Test
    void throwsWhenOpponentIsInactive() {
        defender.setStatus(DigimonStatus.STORED);
        stubDigimons();

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
    }

    @Test
    void throwsWhenOpponentOutOfRatingWindow() {
        defender.setArenaRating(1000 + ArenaRules.RATING_WINDOW + 1);
        stubDigimons();

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
    }

    @Test
    void botDefenderIgnoresRatingWindow() {
        defender = digimon(defenderId, opponentPlayerId, 1000 + ArenaRules.RATING_WINDOW + 500, Stage.ROOKIE, true);
        stubDigimons();
        stubPowers(1000.0, 100.0);

        assertDoesNotThrow(() -> challengeWithRoll(1));
    }

    @Test
    void throwsWhenOpponentStageTooFar() {
        defender.setStage(Stage.MEGA);
        attacker.setStage(Stage.BABY);
        stubDigimons();

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
    }

    @Test
    void throwsWhenDailyLimitReached() {
        stubDigimons();
        when(arenaMatchRepository.countByAttackerPlayerIdAndCreatedAtGreaterThanEqual(eq(playerId), any()))
                .thenReturn((long) ArenaRules.DAILY_CHALLENGE_LIMIT);

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
        verify(arenaMatchRepository, never()).save(any());
    }

    @Test
    void throwsWhenTargetOnCooldown() {
        stubDigimons();
        ArenaMatch recent = ArenaMatch.builder()
                .id(UUID.randomUUID())
                .attackerPlayerId(playerId)
                .attackerDigimonId(attackerId)
                .defenderPlayerId(opponentPlayerId)
                .defenderDigimonId(defenderId)
                .createdAt(Instant.now())
                .build();
        when(arenaMatchRepository
                .findFirstByAttackerPlayerIdAndDefenderDigimonIdOrderByCreatedAtDesc(playerId, defenderId))
                .thenReturn(Optional.of(recent));

        assertThrows(BadRequestException.class, () -> useCase.execute(token, defenderId));
        verify(arenaMatchRepository, never()).save(any());
    }

    @Test
    void adminIgnoresDailyLimitAndCooldown() {
        player.setUserType(UserType.ADMIN);
        stubDigimons();
        stubPowers(1000.0, 100.0);

        ArenaMatchResponse response = challengeWithRoll(1);

        assertTrue(response.victory());
        verify(arenaMatchRepository, never())
                .countByAttackerPlayerIdAndCreatedAtGreaterThanEqual(any(), any());
        verify(arenaMatchRepository, never())
                .findFirstByAttackerPlayerIdAndDefenderDigimonIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void concurrentDefenderUpdateThrowsConflict() {
        stubDigimons();
        stubPowers(1000.0, 100.0);
        doThrow(new ObjectOptimisticLockingFailureException(Digimon.class, defenderId))
                .when(digimonRepository).flush();

        assertThrows(ConflictException.class, () -> challengeWithRoll(1));
        verify(arenaMatchRepository, never()).save(any());
    }

    @Test
    void persistsMatchWithAttackerAndDefenderIds() {
        stubDigimons();
        stubPowers(1000.0, 100.0);

        challengeWithRoll(1);

        ArgumentCaptor<ArenaMatch> captor = ArgumentCaptor.forClass(ArenaMatch.class);
        verify(arenaMatchRepository).save(captor.capture());
        ArenaMatch saved = captor.getValue();
        assertEquals(playerId, saved.getAttackerPlayerId());
        assertEquals(attackerId, saved.getAttackerDigimonId());
        assertEquals(defenderId, saved.getDefenderDigimonId());
        assertEquals(opponentPlayerId, saved.getDefenderPlayerId());
    }
}
