package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionStartResponse;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class StartMissionUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final ClanBonusService clanBonusService;
    private final GameplayConfig gameplayConfig;
    private static final long COOLDOWN_SECONDS = 10;

    @Transactional
    public MissionStartResponse execute(String token, String missionId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = getActiveDigimon(player);
        MissionDefinitionEntity entity = missionDefinitionRepository.findById(missionId).orElseThrow(() -> new NotFoundException("Mission not found"));
        MissionDefinition mission = MissionDefinitionMapper.toDefinition(entity);
        Stage highestStage = getHighestStage(playerId);
        if (!AreaRules.isUnlocked(highestStage, mission.getArea())) {
            throw new BadRequestException("Area locked: " + mission.getArea());
        }
        validateRequirement(digimon, mission);
        boolean isAdmin = player.getUserType() == UserType.ADMIN;
        // \ud83d\udd0b Energia
        UUID clanId = player.getClanId();
        if (!isAdmin && gameplayConfig.isEnergyConsumptionEnabled()) {
            int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;
            digimon.regenerateEnergy(maxEnergyBonus);
            int energyCost = clanId != null ? applyCostReduction(mission.getEnergyCost(), clanBonusService.getEnergyCostMultiplier(clanId)) : mission.getEnergyCost();
            if (digimon.getEnergy() < energyCost) {
                throw new UnprocessableException("Energia insuficiente");
            }
            digimon.consumeEnergy(energyCost);
        }
        // \ud83d\udd12 Verificar se já está em missão
        boolean alreadyRunning = missionInstanceRepository.existsByDigimonIdAndStatus(digimon.getId(), MissionStatus.RUNNING);
        if (alreadyRunning) {
            throw new ConflictException("Digimon já está em missão");
        }
        digimonRepository.save(digimon);
        // ⏱ Criar instância
        Duration missionDuration = isAdmin ? Duration.ZERO : Duration.ofSeconds(mission.getDurationSeconds());
        MissionInstance instance = new MissionInstance(playerId, digimon.getId(), missionId, missionDuration);
        missionInstanceRepository.save(instance);
        return new MissionStartResponse(instance.getId(), instance.getEndsAt());
    }

    private Stage getHighestStage(UUID playerId) {
        return digimonRepository.findByPlayerId(playerId).stream().map(Digimon::getStage).max(Enum::compareTo).orElse(Stage.BABY);
    }

    private int applyCostReduction(int baseCost, double multiplier) {
        if (multiplier >= 1.0) return baseCost;
        return Math.max(1, (int) Math.floor(baseCost * multiplier));
    }

    private void validateCooldown(Player player) {
        if (player.getLastMissionAt() == null) return;
        long seconds = Duration.between(player.getLastMissionAt(), LocalDateTime.now()).getSeconds();
        if (seconds < COOLDOWN_SECONDS) {
            throw new ConflictException("Mission on cooldown. Try again in " + (COOLDOWN_SECONDS - seconds) + " seconds.");
        }
    }

    private Digimon getActiveDigimon(Player player) {
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        return digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
    }

    private void validateRequirement(Digimon digimon, MissionDefinition mission) {
        if (digimon.getStage().ordinal() < mission.getRequiredStage().ordinal()) {
            throw new BadRequestException("Mission locked: stage too low");
        }
        if (digimon.getLevel() < mission.getRequiredLevel()) {
            throw new BadRequestException("Mission locked: level too low");
        }
    }

    public StartMissionUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final MissionInstanceRepository missionInstanceRepository, final MissionDefinitionRepository missionDefinitionRepository, final ClanBonusService clanBonusService, final GameplayConfig gameplayConfig) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.clanBonusService = clanBonusService;
        this.gameplayConfig = gameplayConfig;
    }
}
