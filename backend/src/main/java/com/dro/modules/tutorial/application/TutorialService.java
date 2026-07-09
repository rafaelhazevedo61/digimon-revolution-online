package com.dro.modules.tutorial.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.api.dto.response.TutorialProgressResponse;
import com.dro.modules.tutorial.api.dto.response.TutorialStepResponse;
import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.modules.tutorial.infra.TutorialProgressRepository;
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

@Service
@RequiredArgsConstructor
public class TutorialService {

    private final TutorialProgressRepository tutorialProgressRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    /**
     * Marca um step do tutorial como concluído e concede a recompensa.
     * Idempotente: se o step já foi concluído, nada acontece (sem recompensa dupla).
     * Nunca lança exceção que interrompa o fluxo principal do jogador.
     */
    @Transactional
    public void completeStep(UUID playerId, TutorialStep step) {
        try {
            if (tutorialProgressRepository.existsByPlayerIdAndStep(playerId, step)) {
                return;
            }

            tutorialProgressRepository.save(TutorialProgress.builder()
                    .id(UUID.randomUUID())
                    .playerId(playerId)
                    .step(step)
                    .completedAt(LocalDateTime.now())
                    .build());

            grantReward(playerId, step);
        } catch (RuntimeException ignored) {
            // O tutorial é um bônus; falhas aqui não devem quebrar a ação principal.
        }
    }

    private void grantReward(UUID playerId, TutorialStep step) {
        if (step.getRewardBits() <= 0 && !step.hasItemReward()) {
            return;
        }

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null || player.getActiveDigimonId() == null) {
            return;
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElse(null);
        if (digimon == null) {
            return;
        }

        if (step.getRewardBits() > 0) {
            digimon.setBits(digimon.getBits() + step.getRewardBits());
            digimonRepository.save(digimon);
        }

        if (step.hasItemReward()) {
            addItemUseCase.execute(digimon.getId(), step.getRewardItem(), step.getRewardItemQuantity());
        }
    }

    @Transactional(readOnly = true)
    public TutorialProgressResponse getProgress(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Set<TutorialStep> completed = tutorialProgressRepository.findByPlayerId(playerId).stream()
                .map(TutorialProgress::getStep)
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(TutorialStep.class)));

        List<TutorialStepResponse> steps = Arrays.stream(TutorialStep.values())
                .sorted(Comparator.comparingInt(TutorialStep::getOrder))
                .map(step -> new TutorialStepResponse(
                        step.name(),
                        step.getOrder(),
                        step.getTitle(),
                        step.getDescription(),
                        step.getRewardBits(),
                        step.hasItemReward() ? step.getRewardItem().name() : null,
                        step.getRewardItemQuantity(),
                        completed.contains(step)
                ))
                .toList();

        int total = TutorialStep.values().length;
        int done = completed.size();

        return new TutorialProgressResponse(done, total, done >= total, steps);
    }
}
