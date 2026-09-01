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
import com.dro.modules.mission.domain.MissionTeam;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private MissionTeamRepository missionTeamRepository;
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
                missionTeamRepository,
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
    void blocksHighStageMissionForBabyActiveDigimonEvenWhenAnotherDigimonIsMega() {
        Digimon activeBaby = digimonWithEnergy(10, Stage.BABY);
        stubMissionStart(activeBaby);
        when(missionDefinitionRepository.findById("mission-1"))
                .thenReturn(Optional.of(missionDefinition(Area.INFINITY_MOUNTAIN, Stage.MEGA)));
        assertThrows(BadRequestException.class, () -> useCase.execute(token, "mission-1"));

        verify(digimonRepository, never()).findByPlayerId(playerId);
        verify(digimonRepository, never()).save(activeBaby);
        verify(missionInstanceRepository, never()).save(any(MissionInstance.class));
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

    @Test
    void startsMissionWithTheWholeTeamInOneSlot() {
        UUID teamId = UUID.randomUUID();
        UUID secondDigimonId = UUID.randomUUID();
        UUID thirdDigimonId = UUID.randomUUID();
        Digimon first = digimonWithEnergy(10);
        Digimon second = teamDigimon(secondDigimonId, 10);
        Digimon third = teamDigimon(thirdDigimonId, 10);
        MissionTeam team = mock(MissionTeam.class);
        List<UUID> teamDigimonIds = List.of(digimonId, secondDigimonId, thirdDigimonId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(missionTeamRepository.findByIdAndPlayerId(teamId, playerId)).thenReturn(Optional.of(team));
        when(team.getDigimonIds()).thenReturn(teamDigimonIds);
        when(digimonRepository.findAllByIdForUpdate(playerId, teamDigimonIds))
                .thenReturn(List.of(first, second, third));
        when(missionDefinitionRepository.findById("mission-1"))
                .thenReturn(Optional.of(missionDefinition()));
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);

        useCase.execute(token, "mission-1", teamId);

        ArgumentCaptor<MissionInstance> captor = ArgumentCaptor.forClass(MissionInstance.class);
        verify(missionInstanceRepository).save(captor.capture());
        assertEquals(teamId, captor.getValue().getTeamId());
        assertEquals(teamDigimonIds, captor.getValue().getDigimonIds());
        verify(digimonRepository).save(first);
        verify(digimonRepository).save(second);
        verify(digimonRepository).save(third);
    }

    @Test
    void startsMissionWithPartialTeamInOneSlot() {
        UUID teamId = UUID.randomUUID();
        UUID secondDigimonId = UUID.randomUUID();
        Digimon first = digimonWithEnergy(10);
        Digimon second = teamDigimon(secondDigimonId, 10);
        MissionTeam team = mock(MissionTeam.class);
        List<UUID> teamDigimonIds = List.of(digimonId, secondDigimonId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(missionTeamRepository.findByIdAndPlayerId(teamId, playerId)).thenReturn(Optional.of(team));
        when(team.getDigimonIds()).thenReturn(teamDigimonIds);
        when(digimonRepository.findAllByIdForUpdate(playerId, teamDigimonIds))
                .thenReturn(List.of(first, second));
        when(missionDefinitionRepository.findById("mission-1"))
                .thenReturn(Optional.of(missionDefinition()));
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);

        useCase.execute(token, "mission-1", teamId);

        ArgumentCaptor<MissionInstance> captor = ArgumentCaptor.forClass(MissionInstance.class);
        verify(missionInstanceRepository).save(captor.capture());
        assertEquals(teamDigimonIds, captor.getValue().getDigimonIds());
    }

    private Digimon teamDigimon(UUID id, int energy) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .name("Teammon")
                .type("Vaccine")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .energy(energy)
                .maxEnergy(energy)
                .lastEnergyUpdate(Instant.now())
                .grade(com.dro.modules.digimon.domain.enums.DigimonGrade.C)
                .rarity(com.dro.modules.digimon.domain.enums.Rarity.COMMON)
                .personality(com.dro.modules.digimon.domain.enums.Personality.FIGHTER)
                .status(com.dro.modules.digimon.domain.enums.DigimonStatus.HATCHED)
                .build();
    }

    private void stubMissionStart(Digimon digimon) {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(missionDefinitionRepository.findById("mission-1")).thenReturn(Optional.of(missionDefinition()));
    }

    private Digimon digimonWithEnergy(int energy) {
        return digimonWithEnergy(energy, Stage.ROOKIE);
    }

    private Digimon digimonWithEnergy(int energy, Stage stage) {
        return Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
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
        return missionDefinition(Area.NATIVE_FOREST, Stage.BABY);
    }

    private MissionDefinitionEntity missionDefinition(Area area, Stage requiredStage) {
        return MissionDefinitionEntity.builder()
                .id("mission-1")
                .name("Test Mission")
                .description("Mission used by unit tests")
                .area(area)
                .requiredStage(requiredStage)
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
