package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionStartResponse;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
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
    private final MissionTeamRepository missionTeamRepository;
    private final ClanBonusService clanBonusService;
    private final GameplayConfig gameplayConfig;
    private static final long COOLDOWN_SECONDS = 10;

    @Transactional
    public MissionStartResponse execute(String token, String missionId) {
        return execute(token, missionId, null, false);
    }

    @Transactional
    public MissionStartResponse execute(String token, String missionId, UUID teamId) {
        return execute(token, missionId, teamId, false);
    }

    @Transactional
    public MissionStartResponse execute(String token, String missionId, UUID teamId, boolean autoRepeat) {
        return execute(token, missionId, teamId, autoRepeat, false);
    }

    @Transactional
    public MissionStartResponse execute(String token, String missionId, UUID teamId, boolean autoRepeat, boolean autoClaim) {
        return executeForPlayer(TokenExtractor.extractPlayerId(token), missionId, teamId, autoRepeat, autoClaim);
    }

    @Transactional
    public MissionStartResponse executeForPlayer(UUID playerId, String missionId, UUID teamId, boolean autoRepeat, boolean autoClaim) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        List<Digimon> digimons = teamId == null
                ? List.of(getActiveDigimon(player))
                : getTeamDigimons(playerId, teamId);
        MissionDefinitionEntity entity = missionDefinitionRepository.findById(missionId).orElseThrow(() -> new NotFoundException("Mission not found"));
        MissionDefinition mission = MissionDefinitionMapper.toDefinition(entity);
        digimons.forEach(digimon -> {
            if (!AreaRules.isUnlocked(digimon.getStage(), mission.getArea())) {
                throw new BadRequestException("Area locked: " + mission.getArea());
            }
            validateRequirement(digimon, mission);
        });

        boolean isAdmin = player.getUserType() == UserType.ADMIN;
        int availableMissionSlots = isAdmin
                ? MissionSlotRules.TOTAL_SLOTS
                : MissionSlotRules.normalizeUnlockedSlots(player.getUnlockedMissionSlots());
        Set<Integer> occupiedMissionSlots = new HashSet<>();
        missionInstanceRepository.findByPlayerIdAndStatusIn(
                playerId,
                List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
        ).forEach(instance -> occupiedMissionSlots.add(instance.getSlotNumber()));
        int slotNumber = java.util.stream.IntStream.rangeClosed(1, availableMissionSlots)
                .filter(candidate -> !occupiedMissionSlots.contains(candidate))
                .findFirst()
                .orElseThrow(() -> new ConflictException(isAdmin
                        ? "Todos os slots de missão estão ocupados"
                        : "Todos os slots de missão desbloqueados estão ocupados"));

        UUID clanId = player.getClanId();
        if (!isAdmin && gameplayConfig.isEnergyConsumptionEnabled()) {
            int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;
            int energyCost = clanId != null
                    ? applyCostReduction(mission.getEnergyCost(), clanBonusService.getEnergyCostMultiplier(clanId))
                    : mission.getEnergyCost();
            for (Digimon digimon : digimons) {
                digimon.regenerateEnergy(maxEnergyBonus);
                if (digimon.getEnergy() < energyCost) {
                    throw new UnprocessableException("Energia insuficiente para todo o time");
                }
            }
            digimons.forEach(digimon -> digimon.consumeEnergy(energyCost));
        }

        List<UUID> digimonIds = digimons.stream().map(Digimon::getId).toList();
        boolean alreadyRunning = teamId == null
                ? missionInstanceRepository.existsByDigimonIdAndStatus(digimonIds.get(0), MissionStatus.RUNNING)
                : missionInstanceRepository.existsByPlayerIdAndAnyDigimonIdAndStatusIn(
                        playerId,
                        digimonIds,
                        List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
                );
        if (alreadyRunning) {
            throw new ConflictException("Um ou mais Digimons do time já estão em missão");
        }

        digimons.forEach(digimonRepository::save);
        Duration missionDuration = isAdmin ? Duration.ZERO : Duration.ofSeconds(mission.getDurationSeconds());
        MissionInstance instance = new MissionInstance(playerId, teamId, slotNumber, digimonIds, missionId, missionDuration);
        instance.setAutoRepeatEnabled(autoRepeat && teamId != null);
        instance.setAutoClaimEnabled(autoClaim && teamId != null);
        missionInstanceRepository.save(instance);
        return new MissionStartResponse(instance.getId(), instance.getEndsAt());
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

    private List<Digimon> getTeamDigimons(UUID playerId, UUID teamId) {
        if (missionTeamRepository == null) {
            throw new BadRequestException("Seleção de time indisponível");
        }
        MissionTeam team = missionTeamRepository.findByIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        List<UUID> digimonIds = team.getDigimonIds();
        List<Digimon> digimons = digimonRepository.findAllByIdForUpdate(playerId, digimonIds);
        if (digimons.size() != digimonIds.size()) {
            throw new ConflictException("Um ou mais Digimons do time não estão disponíveis");
        }
        return digimonIds.stream()
                .map(id -> digimons.stream().filter(digimon -> digimon.getId().equals(id)).findFirst()
                        .orElseThrow(() -> new NotFoundException("Digimon do time não encontrado")))
                .toList();
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
        this(playerRepository, digimonRepository, missionInstanceRepository, missionDefinitionRepository, null, clanBonusService, gameplayConfig);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public StartMissionUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final MissionInstanceRepository missionInstanceRepository, final MissionDefinitionRepository missionDefinitionRepository, final MissionTeamRepository missionTeamRepository, final ClanBonusService clanBonusService, final GameplayConfig gameplayConfig) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.missionTeamRepository = missionTeamRepository;
        this.clanBonusService = clanBonusService;
        this.gameplayConfig = gameplayConfig;
    }
}
