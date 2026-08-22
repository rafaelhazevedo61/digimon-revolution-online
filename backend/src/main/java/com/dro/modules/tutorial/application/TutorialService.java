package com.dro.modules.tutorial.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.api.dto.response.TutorialProgressResponse;
import com.dro.modules.tutorial.api.dto.response.TutorialStepResponse;
import com.dro.modules.tutorial.domain.TutorialCompletion;
import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.modules.tutorial.infra.TutorialCompletionRepository;
import com.dro.modules.tutorial.infra.TutorialProgressRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorialService {

    private final TutorialProgressRepository tutorialProgressRepository;
    private final TutorialCompletionRepository tutorialCompletionRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;
    private final ItemDefinitionRepository itemDefinitionRepository;

    /**
     * Marca uma etapa como concluída, mas deixa sua recompensa pendente de resgate.
     * Etapas sem recompensa são consideradas resgatadas no próprio momento da conclusão.
     * Falhas no tutorial não interrompem a ação principal que o jogador realizou.
     */
    @Transactional
    public void completeStep(UUID playerId, TutorialStep step) {
        try {
            if (tutorialProgressRepository.existsByPlayerIdAndStep(playerId, step)) {
                return;
            }

            LocalDateTime completedAt = LocalDateTime.now();
            tutorialProgressRepository.save(TutorialProgress.builder()
                    .id(UUID.randomUUID())
                    .playerId(playerId)
                    .step(step)
                    .completedAt(completedAt)
                    .rewardClaimedAt(step.hasReward() ? null : completedAt)
                    .build());
        } catch (RuntimeException ignored) {
            // O tutorial é um bônus; falhas aqui não devem quebrar a ação principal.
        }
    }

    @Transactional
    public TutorialProgressResponse claimReward(String token, String stepName) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        TutorialStep step = parseStep(stepName);

        TutorialProgress progress = tutorialProgressRepository.findByPlayerIdForUpdate(playerId).stream()
                .filter(item -> item.getStep() == step)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("A etapa do tutorial ainda não foi concluída"));

        if (progress.getRewardClaimedAt() == null) {
            grantReward(playerId, step);
            progress.setRewardClaimedAt(LocalDateTime.now());
            tutorialProgressRepository.save(progress);
        }

        return getProgress(token);
    }

    @Transactional
    public TutorialProgressResponse finishTutorial(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        if (tutorialCompletionRepository.existsById(playerId)) {
            return getProgress(token);
        }

        List<TutorialProgress> progress = tutorialProgressRepository.findByPlayerIdForUpdate(playerId);
        Set<TutorialStep> completed = progress.stream()
                .map(TutorialProgress::getStep)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TutorialStep.class)));

        if (completed.size() < TutorialStep.values().length) {
            throw new BadRequestException("Conclua todas as etapas do tutorial antes de finalizá-lo");
        }

        boolean hasPendingReward = progress.stream()
                .anyMatch(item -> item.getStep().hasReward() && item.getRewardClaimedAt() == null);
        if (hasPendingReward) {
            throw new BadRequestException("Resgate todas as recompensas do tutorial antes de finalizá-lo");
        }

        tutorialCompletionRepository.save(TutorialCompletion.builder()
                .playerId(playerId)
                .finishedAt(LocalDateTime.now())
                .build());

        return getProgress(token);
    }

    private void grantReward(UUID playerId, TutorialStep step) {
        if (!step.hasReward()) {
            return;
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("Nenhum Digimon ativo selecionado");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon ativo não encontrado"));

        if (step.getRewardBits() > 0) {
            digimon.setBits(digimon.getBits() + step.getRewardBits());
            digimonRepository.save(digimon);
        }

        if (step.hasItemReward()) {
            addItemUseCase.execute(digimon.getId(), step.getRewardItem(), step.getRewardItemQuantity());
        }
    }

    private String findRewardItemName(TutorialStep step) {
        if (!step.hasItemReward()) {
            return null;
        }

        try {
            return itemDefinitionRepository.findByCode(step.getRewardItem().name())
                    .map(ItemDefinition::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse(null);
        } catch (RuntimeException ignored) {
            // A catalog lookup is presentation-only and must not break the tutorial.
            return null;
        }
    }

    private TutorialStep parseStep(String stepName) {
        try {
            return TutorialStep.valueOf(stepName.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BadRequestException("Etapa de tutorial desconhecida: " + stepName);
        }
    }

    @Transactional(readOnly = true)
    public TutorialProgressResponse getProgress(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        List<TutorialProgress> progress = tutorialProgressRepository.findByPlayerId(playerId);

        Set<TutorialStep> completed = progress.stream()
                .map(TutorialProgress::getStep)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TutorialStep.class)));

        Set<TutorialStep> claimed = progress.stream()
                .filter(item -> item.getRewardClaimedAt() != null)
                .map(TutorialProgress::getStep)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TutorialStep.class)));

        List<TutorialStepResponse> steps = Arrays.stream(TutorialStep.values())
                .sorted(Comparator.comparingInt(TutorialStep::getOrder))
                .map(step -> new TutorialStepResponse(
                        step.name(),
                        step.getOrder(),
                        step.getTitle(),
                        step.getDescription(),
                        step.getRewardBits(),
                        step.hasItemReward() ? step.getRewardItem().name() : null,
                        findRewardItemName(step),
                        step.getRewardItemQuantity(),
                        completed.contains(step),
                        claimed.contains(step)
                ))
                .toList();

        int total = TutorialStep.values().length;
        int completedSteps = completed.size();
        int rewardSteps = (int) Arrays.stream(TutorialStep.values()).filter(TutorialStep::hasReward).count();
        int claimedRewards = (int) progress.stream()
                .filter(item -> item.getStep().hasReward() && item.getRewardClaimedAt() != null)
                .count();
        int pendingRewards = (int) progress.stream()
                .filter(item -> item.getStep().hasReward() && item.getRewardClaimedAt() == null)
                .count();
        boolean allCompleted = completedSteps >= total;
        boolean finished = tutorialCompletionRepository.existsById(playerId);
        boolean canFinish = allCompleted && claimedRewards >= rewardSteps && !finished;

        return new TutorialProgressResponse(
                completedSteps,
                total,
                allCompleted,
                claimedRewards,
                pendingRewards,
                canFinish,
                finished,
                steps
        );
    }
}
