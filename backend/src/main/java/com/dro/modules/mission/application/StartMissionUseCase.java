package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.api.MissionResultResponse;
import com.dro.modules.mission.domain.MissionCatalog;
import com.dro.modules.mission.domain.MissionDefinition;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMissionUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    private static final long COOLDOWN_SECONDS = 10;

    public MissionResultResponse execute(String token, String missionId) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        LocalDateTime now = LocalDateTime.now();

        // 🔥 Cooldown fixo por enquanto
        if (player.getLastMissionAt() != null) {

            long secondsSinceLastMission =
                    Duration.between(player.getLastMissionAt(), now).getSeconds();

            if (secondsSinceLastMission < COOLDOWN_SECONDS) {
                throw new RuntimeException(
                        "Mission on cooldown. Try again in "
                                + (COOLDOWN_SECONDS - secondsSinceLastMission)
                                + " seconds."
                );
            }
        }

        Digimon digimon = digimonRepository
                .findById(player.getActiveDigimonId())
                .orElseThrow(() -> new RuntimeException("Active digimon not found"));

        // 🔎 Buscar missão no catálogo
        MissionDefinition mission = MissionCatalog.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        // 🔐 Validar requisito de nível
        if (digimon.getLevel() < mission.getRequiredLevel()) {
            throw new RuntimeException("Mission locked: level too low");
        }

        int previousLevel = digimon.getLevel();

        // 🎯 Aplicar XP
        int xpGained = mission.getXpReward();
        digimon.gainExperience(xpGained);

        boolean levelUp = digimon.getLevel() > previousLevel;

        digimonRepository.save(digimon);

        // 🎁 Aplicar recompensa
        ItemType droppedItem = mission.getRewardItem();
        if (droppedItem != null) {
            addItemUseCase.execute(playerId, droppedItem, 1);
        }

        player.setLastMissionAt(now);
        playerRepository.save(player);

        return new MissionResultResponse(
                mission.getId(),
                xpGained,
                levelUp,
                droppedItem
        );
    }
}
