package com.dro.modules.mission.application;

import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mission.domain.MissionRewardEntity;
import com.dro.modules.mission.api.dto.response.MissionResultResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.domain.PlayerMissionProgress;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.PlayerMissionProgressRepository;
import com.dro.shared.gameplay.WeekendDoubleRewardRules;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimMissionUseCaseTest {

    @Mock
    private MissionInstanceRepository missionInstanceRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private DigimonInfosRepository digimonInfosRepository;

    @Mock
    private PlayerMissionProgressRepository progressRepository;

    @Mock
    private AddItemUseCase addItemUseCase;

    @Mock
    private MissionDefinitionRepository missionDefinitionRepository;

    @Mock
    private TutorialService tutorialService;

    @Mock
    private ClanBonusService clanBonusService;

    @Mock
    private ClanMissionProgressTracker clanMissionProgressTracker;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ChestDefinitionRepository chestDefinitionRepository;

    @Mock
    private ItemDefinitionRepository itemDefinitionRepository;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    @InjectMocks
    private ClaimMissionUseCase claimMissionUseCase;

    @Test
    void executeDeliversAreaChestInsteadOfLegacyRandomItemAndIgnoresLegacyFixedRewards() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        String missionId = "MISSION_1";
        String chestCode = "CHEST_MISSION_MISSION_1";

        MissionInstance instance = new MissionInstance(
                playerId,
                teamId,
                List.of(digimonId),
                missionId,
                Duration.ZERO
        );
        MissionDefinitionEntity mission = missionDefinition(missionId, chestCode);
        PlayerMissionProgress progress = PlayerMissionProgress.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .missionId(missionId)
                .completionCount(0)
                .build();
        Digimon digimon = digimon(digimonId, playerId);
        digimon.setExperience(90);
        Long digimonInfoId = 100L;
        digimon.setDigimonInfoId(digimonInfoId);
        when(digimonInfosRepository.findById(digimonInfoId)).thenReturn(Optional.of(
                DigimonInfos.builder().id(digimonInfoId).imageUrl("https://example.test/agumon.png").build()
        ));
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(digimonId)
                .build();
        ChestDefinitionEntity chest = chest(chestCode);

        when(missionInstanceRepository.findByIdAndPlayerId(any(UUID.class), eq(playerId)))
                .thenReturn(Optional.of(instance));
        when(missionDefinitionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(progressRepository.findByPlayerIdAndMissionId(playerId, missionId))
                .thenReturn(Optional.of(progress));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(chestDefinitionRepository.findWithCatalogByCode(chestCode))
                .thenReturn(Optional.of(chest));
        WeekendDoubleRewardRules.setManualOverride(true, Instant.now());
        MissionResultResponse response;
        try {
            response = claimMissionUseCase.execute(
                    token(playerId),
                    UUID.randomUUID()
            );
        } finally {
            WeekendDoubleRewardRules.setManualOverride(null, Instant.now());
        }

        assertThat(response.missionId()).isEqualTo(missionId);
        assertThat(response.teamId()).isEqualTo(teamId);
        assertThat(response.digimonExperience()).hasSize(1);
        assertThat(response.digimonExperience().get(0).id()).isEqualTo(digimonId);
        assertThat(response.digimonExperience().get(0).name()).isEqualTo("Agumon");
        assertThat(response.digimonExperience().get(0).imageUrl()).isEqualTo("https://example.test/agumon.png");
        assertThat(response.digimonExperience().get(0).level()).isEqualTo(2);
        assertThat(response.digimonExperience().get(0).levelsGained()).isEqualTo(1);
        assertThat(response.digimonExperience().get(0).experience()).isEqualTo(50);
        assertThat(response.digimonExperience().get(0).experienceToNextLevel()).isEqualTo(200);
        assertThat(response.digimonExperience().get(0).experiencePercent()).isEqualTo(25.0);
        assertThat(response.xpGained()).isEqualTo(60);
        assertThat(response.bitsGained()).isEqualTo(0);
        assertThat(response.experienceBreakdown().baseAmount()).isEqualTo(30);
        assertThat(response.experienceBreakdown().eventMultiplier()).isEqualTo(2);
        assertThat(response.experienceBreakdown().amountBeforeDigimonMultiplier()).isEqualTo(60);
        assertThat(response.experienceBreakdown().combinedMultiplier()).isEqualTo(2.0);
        assertThat(response.experienceBreakdown().effectiveMultiplier()).isEqualTo(2.0);
        assertThat(response.experienceBreakdown().finalAmount()).isEqualTo(60);
        assertThat(response.rewards()).hasSize(1);
        assertThat(response.rewards().get(0).item()).isEqualTo(ItemType.LOOT_CHEST);
        assertThat(response.rewards().get(0).quantity()).isEqualTo(1);
        assertThat(response.rewards().get(0).itemCode()).isEqualTo(chest.getCode());
        assertThat(response.rewards().get(0).itemName()).isEqualTo(chest.getName());

        verifyNoInteractions(itemDefinitionRepository);
        verify(addItemUseCase).addMaterial(digimonId, chest.getItemDefinition(), 1);
        verify(addItemUseCase, never()).execute(eq(digimonId), any(ItemType.class), anyInt());
        verify(missionInstanceRepository).save(instance);
        verify(progressRepository).save(progress);
        verify(digimonRepository).save(digimon);
        verify(transactionAuditPublisher).success(
                argThat(value -> value.startsWith("mission-claim:")),
                eq("MISSION_CLAIMED"),
                eq("MissionInstance"),
                any(String.class),
                any()
        );
        assertThat(instance.getStatus()).isEqualTo(MissionStatus.CLAIMED);
    }

    private MissionDefinitionEntity missionDefinition(String id, String chestCode) {
        ChestDefinitionEntity chest = chest(chestCode);
        return MissionDefinitionEntity.builder()
                .id(id)
                .name("Patrulha na Floresta Nativa")
                .description("Missão de teste")
                .area(com.dro.modules.mission.domain.Area.NATIVE_FOREST)
                .requiredStage(Stage.BABY)
                .requiredLevel(1)
                .baseXp(30)
                .baseBits(0)
                .energyCost(5)
                .durationSeconds(1)
                .active(true)
                .createdBy("TEST")
                .updatedBy("TEST")
                .chestDefinition(chest)
                .rewards(List.of(MissionRewardEntity.builder()
                        .itemType(ItemType.TRAINING_STONE)
                        .baseQuantity(1)
                        .build()))
                .build();
    }

    private ChestDefinitionEntity chest(String code) {
        return ChestDefinitionEntity.builder()
                .id(1L)
                .code(code)
                .name("Baú Floresta Nativa")
                .itemDefinition(ItemDefinition.builder()
                        .id(100L)
                        .code(code)
                        .name("Baú Floresta Nativa")
                        .category("CHEST")
                        .maxStack(999)
                        .build())
                .active(true)
                .tradable(true)
                .build();
    }

    private Digimon digimon(UUID digimonId, UUID playerId) {
        return Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("Vaccine")
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(100)
                .maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .build();
    }

    private String token(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
    }
}
