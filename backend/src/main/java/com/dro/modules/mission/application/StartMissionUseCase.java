package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.api.MissionResultResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    private final Random random = new Random();

    public MissionResultResponse execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        int previousLevel = digimon.getLevel();

        // 🎯 XP base
        int xpGained = 50;
        digimon.gainExperience(xpGained);

        boolean levelUp = digimon.getLevel() > previousLevel;

        digimonRepository.save(digimon);

        // 🎲 Sistema de Drop
        int roll = random.nextInt(100);
        ItemType droppedItem = null;

        if (roll < 3) {
            droppedItem = ItemType.INCUBATOR_RARE;
        } else if (roll < 10) {
            droppedItem = ItemType.INCUBATOR_COMMON;
        } else if (roll < 20) {
            droppedItem = ItemType.DIGITAMA_FIRE;
        } else if (roll < 50) {
            droppedItem = ItemType.TRAINING_STONE;
        }

        if (droppedItem != null) {
            addItemUseCase.execute(playerId, droppedItem, 1);
        }

        return new MissionResultResponse(
                xpGained,
                levelUp,
                droppedItem
        );
    }
}
