package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.api.MissionResultResponse;
import com.dro.modules.mission.domain.MissionRules;
import com.dro.modules.mission.domain.MissionType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import static com.dro.modules.mission.domain.MissionType.*;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    private final Random random = new Random();

    public MissionResultResponse execute(String token, MissionType type) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        var now = LocalDateTime.now();

        // 🔥 Cooldown dinâmico
        if (player.getLastMissionAt() != null) {

            long secondsSinceLastMission =
                    Duration.between(player.getLastMissionAt(), now).getSeconds();

            long cooldown = MissionRules.getCooldown(type).getSeconds();

            if (secondsSinceLastMission < cooldown) {
                throw new RuntimeException(
                        "Mission on cooldown. Try again in "
                                + (cooldown - secondsSinceLastMission)
                                + " seconds."
                );
            }
        }

        Digimon digimon = digimonRepository
                .findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        int previousLevel = digimon.getLevel();

        int xpGained = MissionRules.getXp(type);
        digimon.gainExperience(xpGained);

        boolean levelUp = digimon.getLevel() > previousLevel;

        digimonRepository.save(digimon);

        // 🎲 Loot escalado
        int roll = random.nextInt(100);
        ItemType droppedItem = null;

        switch (type) {
            case EASY -> {
                if (roll < 30) droppedItem = ItemType.TRAINING_STONE;
            }
            case NORMAL -> {
                if (roll < 20) droppedItem = ItemType.DIGITAMA_FIRE;
                else if (roll < 50) droppedItem = ItemType.TRAINING_STONE;
            }
            case HARD -> {
                if (roll < 5) droppedItem = ItemType.INCUBATOR_RARE;
                else if (roll < 15) droppedItem = ItemType.INCUBATOR_COMMON;
                else if (roll < 40) droppedItem = ItemType.DIGITAMA_FIRE;
            }
        }

        if (droppedItem != null) {
            addItemUseCase.execute(playerId, droppedItem, 1);
        }

        player.setLastMissionAt(now);
        playerRepository.save(player);

        return new MissionResultResponse(
                type,
                xpGained,
                levelUp,
                droppedItem
        );
    }

}
