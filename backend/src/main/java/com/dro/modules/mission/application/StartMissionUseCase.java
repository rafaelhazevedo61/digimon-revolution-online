package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionStartResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
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
    private final MissionInstanceRepository missionInstanceRepository;

    private static final long COOLDOWN_SECONDS = 10;

    public MissionStartResponse execute(String token, String missionId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        Digimon digimon = getActiveDigimon(player);

        MissionDefinition mission = MissionCatalog.findById(missionId)
                .orElseThrow(() -> new NotFoundException("Mission not found"));

        Stage highestStage = getHighestStage(playerId);

        if (!AreaRules.isUnlocked(highestStage, mission.getArea())) {
            throw new BadRequestException("Area locked: " + mission.getArea());
        }

        validateRequirement(digimon, mission);

        // 🔋 Energia
        digimon.regenerateEnergy();

        if (digimon.getEnergy() < mission.getEnergyCost()) {
            throw new UnprocessableException("Energia insuficiente");
        }

        // 🔒 Verificar se já está em missão
        boolean alreadyRunning = missionInstanceRepository
                .existsByDigimonIdAndStatus(digimon.getId(), MissionStatus.RUNNING);

        if (alreadyRunning) {
            throw new ConflictException("Digimon já está em missão");
        }

        digimon.consumeEnergy(mission.getEnergyCost());
        digimonRepository.save(digimon);

        // ⏱ Criar instância
        MissionInstance instance = new MissionInstance(
                playerId,
                digimon.getId(),
                missionId,
                Duration.ofSeconds(mission.getDurationSeconds())
        );

        missionInstanceRepository.save(instance);

        return new MissionStartResponse(
                instance.getId(),
                instance.getEndsAt()
        );
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
            throw new ConflictException("Mission on cooldown. Try again in " + (COOLDOWN_SECONDS - seconds) + " seconds.");
        }
    }

    private Digimon getActiveDigimon (Player player) {

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        return digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
    }

    private void validateRequirement (Digimon digimon, MissionDefinition mission) {
        if (digimon.getLevel() < mission.getRequiredLevel()) {
            throw new BadRequestException("Mission locked: level too low");
        }
    }





}
