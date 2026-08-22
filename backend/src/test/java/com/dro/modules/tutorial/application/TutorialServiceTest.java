package com.dro.modules.tutorial.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.domain.TutorialCompletion;
import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.modules.tutorial.infra.TutorialCompletionRepository;
import com.dro.modules.tutorial.infra.TutorialProgressRepository;
import com.dro.shared.security.JwtTokenCodec;
import com.dro.shared.security.JwtSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorialServiceTest {

    @Mock
    private TutorialProgressRepository tutorialProgressRepository;

    @Mock
    private TutorialCompletionRepository tutorialCompletionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private AddItemUseCase addItemUseCase;

    @InjectMocks
    private TutorialService tutorialService;

    private UUID playerId;
    private UUID digimonId;
    private String token;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = JwtTokenCodec.create(Map.of(
                "sub", playerId.toString(),
                "iss", JwtSettings.getIssuer(),
                "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        ), JwtSettings.getSecret());
    }

    @Test
    void completingRewardStepDoesNotGrantRewardAutomatically() {
        when(tutorialProgressRepository.existsByPlayerIdAndStep(playerId, TutorialStep.COMPLETE_MISSION))
                .thenReturn(false);

        tutorialService.completeStep(playerId, TutorialStep.COMPLETE_MISSION);

        ArgumentCaptor<TutorialProgress> captor = ArgumentCaptor.forClass(TutorialProgress.class);
        verify(tutorialProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getRewardClaimedAt()).isNull();
        verifyNoInteractions(playerRepository, digimonRepository, addItemUseCase);
    }

    @Test
    void claimRewardGrantsBitsAndItemsOnce() {
        TutorialProgress progress = completedProgress(TutorialStep.COMPLETE_MISSION, null);
        Player player = Player.builder().id(playerId).activeDigimonId(digimonId).build();
        Digimon digimon = Digimon.builder().id(digimonId).playerId(playerId).bits(100).build();

        when(tutorialProgressRepository.findByPlayerIdForUpdate(playerId)).thenReturn(List.of(progress));
        when(playerRepository.findById(playerId)).thenReturn(java.util.Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(java.util.Optional.of(digimon));
        when(tutorialProgressRepository.findByPlayerId(playerId)).thenReturn(List.of(progress));
        when(tutorialCompletionRepository.existsById(playerId)).thenReturn(false);

        tutorialService.claimReward(token, TutorialStep.COMPLETE_MISSION.name());

        assertThat(digimon.getBits()).isEqualTo(200);
        verify(addItemUseCase).execute(digimonId, ItemType.POTION_SMALL, 3);
        verify(tutorialProgressRepository).save(progress);
        assertThat(progress.getRewardClaimedAt()).isNotNull();
    }

    @Test
    void repeatedClaimDoesNotGrantRewardAgain() {
        TutorialProgress progress = completedProgress(
                TutorialStep.COMPLETE_MISSION,
                LocalDateTime.now().minusMinutes(1));

        when(tutorialProgressRepository.findByPlayerIdForUpdate(playerId)).thenReturn(List.of(progress));
        when(tutorialProgressRepository.findByPlayerId(playerId)).thenReturn(List.of(progress));
        when(tutorialCompletionRepository.existsById(playerId)).thenReturn(false);

        tutorialService.claimReward(token, TutorialStep.COMPLETE_MISSION.name());

        verifyNoInteractions(playerRepository, digimonRepository, addItemUseCase);
        verify(tutorialProgressRepository, never()).save(progress);
    }

    @Test
    void finishCompletesTutorialAfterAllRewardsAreClaimed() {
        List<TutorialProgress> progress = allCompletedProgressWithPending(null);
        when(tutorialCompletionRepository.existsById(playerId)).thenReturn(false, true);
        when(tutorialProgressRepository.findByPlayerIdForUpdate(playerId)).thenReturn(progress);
        when(tutorialProgressRepository.findByPlayerId(playerId)).thenReturn(progress);

        var result = tutorialService.finishTutorial(token);

        verify(tutorialCompletionRepository).save(any(TutorialCompletion.class));
        assertThat(result.finished()).isTrue();
        assertThat(result.canFinish()).isFalse();
    }

    @Test
    void finishRejectsCompletedTutorialWithPendingReward() {
        List<TutorialProgress> progress = allCompletedProgressWithPending(TutorialStep.COMPLETE_MISSION);
        when(tutorialCompletionRepository.existsById(playerId)).thenReturn(false);
        when(tutorialProgressRepository.findByPlayerIdForUpdate(playerId)).thenReturn(progress);

        assertThatThrownBy(() -> tutorialService.finishTutorial(token))
                .isInstanceOf(com.dro.shared.exception.BadRequestException.class)
                .hasMessageContaining("Claim all tutorial rewards");

        verify(tutorialCompletionRepository, never()).save(any(TutorialCompletion.class));
    }

    private List<TutorialProgress> allCompletedProgressWithPending(TutorialStep pendingStep) {
        return Arrays.stream(TutorialStep.values())
                .map(step -> completedProgress(
                        step,
                        step == pendingStep ? null : LocalDateTime.now().minusMinutes(1)))
                .toList();
    }

    private TutorialProgress completedProgress(TutorialStep step, LocalDateTime rewardClaimedAt) {
        return TutorialProgress.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .step(step)
                .completedAt(LocalDateTime.now().minusMinutes(2))
                .rewardClaimedAt(rewardClaimedAt)
                .build();
    }
}
