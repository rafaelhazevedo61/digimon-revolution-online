package com.dro.modules.mission.application;

import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartMissionUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private DigimonRepository digimonRepository;
    @Mock
    private MissionInstanceRepository missionInstanceRepository;
    @Mock
    private MissionDefinitionRepository missionDefinitionRepository;
    @Mock
    private ClanBonusService clanBonusService;
    @Mock
    private GameplayConfig gameplayConfig;

    private StartMissionUseCase useCase;
    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Player player;

    @BeforeEach
    void setUp() {
        useCase = new StartMissionUseCase(
                playerRepository,
                digimonRepository,
                missionInstanceRepository,
                missionDefinitionRepository,
                clanBonusService,
                gameplayConfig
        );

        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = createToken(playerId);

        player = Player.builder()
                .id(playerId)
                .username("player")
                .email("player@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .userType(UserType.PLAYER)
                .build();
    }

    @Test
    void startsMissionWithZeroEnergyWhenConsumptionIsDisabled() {
        Digimon digimon = digimonWithEnergy(0);
        stubMissionStart(digimon);
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);

        useCase.execute(token, "mission-1");

        assertEquals(0, digimon.getEnergy());
        verify(clanBonusService, never()).getMaxEnergyBonus(any());
        verify(clanBonusService, never()).getEnergyCostMultiplier(any());
        verify(digimonRepository).save(digimon);
        verify(missionInstanceRepository).save(any(MissionInstance.class));
    }

    @Test
    void consumesEnergyWhenConsumptionIsEnabled() {
        Digimon digimon = digimonWithEnergy(10);
        stubMissionStart(digimon);
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(true);

        useCase.execute(token, "mission-1");

        assertEquals(7, digimon.getEnergy());
        verify(digimonRepository).save(digimon);
        verify(missionInstanceRepository).save(any(MissionInstance.class));
    }

    @Test
    void keepsMissionRulesActiveWhenEnergyConsumptionIsDisabled() {
        Digimon digimon = digimonWithEnergy(0);
        stubMissionStart(digimon);
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);
        when(missionInstanceRepository.existsByDigimonIdAndStatus(digimonId, MissionStatus.RUNNING))
                .thenReturn(true);

        assertThrows(
                com.dro.shared.exception.ConflictException.class,
                () -> useCase.execute(token, "mission-1")
        );

        assertEquals(0, digimon.getEnergy());
        verify(missionInstanceRepository, never()).save(any(MissionInstance.class));
    }

    private void stubMissionStart(Digimon digimon) {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(digimonRepository.findByPlayerId(playerId)).thenReturn(java.util.List.of(digimon));
        when(missionDefinitionRepository.findById("mission-1")).thenReturn(Optional.of(missionDefinition()));
        when(missionInstanceRepository.existsByDigimonIdAndStatus(digimonId, MissionStatus.RUNNING))
                .thenReturn(false);
    }

    private Digimon digimonWithEnergy(int energy) {
        return Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(energy)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .arenaRating(1000)
                .arenaWins(0)
                .arenaLosses(0)
                .bot(false)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MissionDefinitionEntity missionDefinition() {
        return MissionDefinitionEntity.builder()
                .id("mission-1")
                .name("Test Mission")
                .description("Mission used by unit tests")
                .area(Area.NATIVE_FOREST)
                .requiredStage(Stage.BABY)
                .requiredLevel(1)
                .baseXp(10)
                .energyCost(3)
                .durationSeconds(60)
                .active(true)
                .build();
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
