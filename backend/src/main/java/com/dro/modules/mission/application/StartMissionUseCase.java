package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.mission.api.response.MissionResultResponse;
import com.dro.modules.mission.api.response.RewardResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.PlayerMissionProgressRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerMissionProgressRepository progressRepository;
    private final AddItemUseCase addItemUseCase;

    private static final long COOLDOWN_SECONDS = 10;

    public MissionResultResponse execute (String token, String missionId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));

        validateCooldown(player);

        Digimon digimon = getActiveDigimon(player);

        MissionDefinition mission = MissionCatalog.findById(missionId).orElseThrow(() -> new RuntimeException("Mission not found"));

        Stage highestStage = getHighestStage(playerId);
        if(!AreaRules.isUnlocked(highestStage, mission.getArea())) {
            throw new RuntimeException("Area locked: " + mission.getArea());
        }

        validateRequirement(digimon, mission);

        digimon.regenerateEnergy();

        if (digimon.getEnergy() < mission.getEnergyCost()) {
            throw new RuntimeException("Energia insuficiente");
        }

        digimon.consumeEnergy(mission.getEnergyCost());

        PlayerMissionProgress progress = getOrCreateProgress(playerId, missionId);

        int completionCount = progress.getCompletionCount();

        int previousLevel = digimon.getLevel();

        int xpGained = calculateScaledXp(mission.getBaseXp(), completionCount);

        digimon.gainExperience(xpGained);
        digimonRepository.save(digimon);

        boolean levelUp = digimon.getLevel() > previousLevel;

        List<RewardResponse> rewards = applyScaledRewards(playerId, mission, completionCount);

        incrementProgress(progress);

        player.setLastMissionAt(LocalDateTime.now());
        playerRepository.save(player);

        return new MissionResultResponse(missionId, xpGained, levelUp, rewards);
    }

    private Stage getHighestStage(UUID playerId) {

        return digimonRepository.findByPlayerId(playerId)
                .stream()
                .map(Digimon::getStage)
                .max(Enum::compareTo)
                .orElse(Stage.BABY);
    }

    private void validateCooldown (Player player) {

        if (player.getLastMissionAt() == null) return;

        long seconds = Duration.between(player.getLastMissionAt(), LocalDateTime.now()).getSeconds();

        if (seconds < COOLDOWN_SECONDS) {
            throw new RuntimeException("Mission on cooldown. Try again in " + (COOLDOWN_SECONDS - seconds) + " seconds.");
        }
    }

    private Digimon getActiveDigimon (Player player) {

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        return digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new RuntimeException("Active digimon not found"));
    }

    private void validateRequirement (Digimon digimon, MissionDefinition mission) {
        if (digimon.getLevel() < mission.getRequiredLevel()) {
            throw new RuntimeException("Mission locked: level too low");
        }
    }

    private PlayerMissionProgress getOrCreateProgress (UUID playerId, String missionId) {
        return progressRepository.findByPlayerIdAndMissionId(playerId, missionId).orElseGet(() -> {
            PlayerMissionProgress progress = PlayerMissionProgress.builder().id(UUID.randomUUID()).playerId(playerId).missionId(missionId).completionCount(0).build();
            return progressRepository.save(progress);
        });
    }

    private int calculateScaledXp (int baseXp, int completionCount) {
        double multiplier = 1 + (completionCount * 0.05);

        return (int) Math.floor(baseXp * multiplier);
    }

    private List<RewardResponse> applyScaledRewards (UUID playerId, MissionDefinition mission, int completionCount) {

        double multiplier = 1 + (completionCount * 0.05);

        List<RewardResponse> rewards = new ArrayList<>();

        for (MissionReward reward : mission.getRewards()) {

            int quantity = (int) Math.floor(reward.getBaseQuantity() * multiplier);

            if (quantity > 0) {

                addItemUseCase.execute(playerId, reward.getItemType(), quantity);

                rewards.add(new RewardResponse(reward.getItemType(), quantity));
            }
        }

        return rewards;
    }

    private void incrementProgress (PlayerMissionProgress progress) {
        progress.setCompletionCount(progress.getCompletionCount() + 1);
        progressRepository.save(progress);
    }
}
