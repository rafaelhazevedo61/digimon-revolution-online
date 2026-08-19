package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.loot.domain.LootItem;
import com.dro.modules.loot.domain.LootRoller;
import com.dro.modules.mission.api.dto.response.MissionResultResponse;
import com.dro.modules.mission.api.dto.response.RewardResponse;
import com.dro.modules.mission.domain.MissionDefinition;
import com.dro.modules.mission.domain.MissionDefinitionMapper;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionReward;
import com.dro.modules.mission.domain.PlayerMissionProgress;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.PlayerMissionProgressRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class ClaimMissionUseCase {

    private final MissionInstanceRepository missionInstanceRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerMissionProgressRepository progressRepository;
    private final AddItemUseCase addItemUseCase;
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final TutorialService tutorialService;
    private final ClanBonusService clanBonusService;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final PlayerRepository playerRepository;

    public ClaimMissionUseCase(
            MissionInstanceRepository missionInstanceRepository,
            DigimonRepository digimonRepository,
            PlayerMissionProgressRepository progressRepository,
            AddItemUseCase addItemUseCase,
            MissionDefinitionRepository missionDefinitionRepository,
            TutorialService tutorialService,
            ClanBonusService clanBonusService,
            ClanMissionProgressTracker clanMissionProgressTracker,
            PlayerRepository playerRepository
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.digimonRepository = digimonRepository;
        this.progressRepository = progressRepository;
        this.addItemUseCase = addItemUseCase;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.tutorialService = tutorialService;
        this.clanBonusService = clanBonusService;
        this.clanMissionProgressTracker = clanMissionProgressTracker;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public MissionResultResponse execute(String token, UUID missionInstanceId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        MissionInstance instance = missionInstanceRepository
                .findByIdAndPlayerId(missionInstanceId, playerId)
                .orElseThrow(() -> new NotFoundException("Missão não encontrada"));

        instance.updateStatusIfFinished();

        if (instance.isAlreadyClaimed()) {
            throw new ConflictException("Missão já foi resgatada");
        }

        if (!instance.canBeClaimed()) {
            throw new BadRequestException("Missão ainda não foi concluída");
        }

        Digimon digimon = digimonRepository.findById(instance.getDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon não encontrado"));

        MissionDefinition mission = MissionDefinitionMapper.toDefinition(
                missionDefinitionRepository.findById(instance.getMissionId())
                        .orElseThrow(() -> new NotFoundException("Mission not found"))
        );

        PlayerMissionProgress progress =
                getOrCreateProgress(playerId, mission.getId());

        int completionCount = progress.getCompletionCount();

        int previousLevel = digimon.getLevel();

        Player player = playerRepository.findById(playerId)
                .orElse(null);
        UUID clanId = player != null ? player.getClanId() : null;

        double xpMultiplier = clanId != null ? clanBonusService.getMissionXpMultiplier(clanId) : 1.0;
        double bitsMultiplier = clanId != null ? clanBonusService.getMissionBitsMultiplier(clanId) : 1.0;

        int xpGained = (int) Math.floor(calculateScaledXp(
                mission.getBaseXp(),
                completionCount
        ) * xpMultiplier);

        digimon.gainExperience(xpGained);

        boolean levelUp = digimon.getLevel() > previousLevel;

        int bitsGained = (int) Math.floor(calculateScaledBits(
                mission.getBaseBits(),
                completionCount
        ) * bitsMultiplier);

        if (bitsGained > 0) {
            digimon.setBits(digimon.getBits() + bitsGained);
        }

        List<RewardResponse> rewards = new ArrayList<>();

        UUID digimonId = instance.getDigimonId();

        rewards.addAll(
                applyFixedRewards(digimonId, mission, completionCount)
        );

        applyRandomLoot(digimonId, mission, rewards);

        incrementProgress(progress);

        instance.markClaimed();

        missionInstanceRepository.save(instance);
        digimonRepository.save(digimon);

        if (clanId != null) {
            clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.MISSIONS_COMPLETED);
        }

        tutorialService.completeStep(playerId, TutorialStep.COMPLETE_MISSION);

        return new MissionResultResponse(
                mission.getId(),
                xpGained,
                bitsGained,
                levelUp,
                rewards
        );
    }

    private PlayerMissionProgress getOrCreateProgress(UUID playerId, String missionId) {
        return progressRepository
                .findByPlayerIdAndMissionId(playerId, missionId)
                .orElseGet(() -> {
                    PlayerMissionProgress progress = PlayerMissionProgress.builder()
                            .id(UUID.randomUUID())
                            .playerId(playerId)
                            .missionId(missionId)
                            .completionCount(0)
                            .build();

                    return progressRepository.save(progress);
                });
    }

    private int calculateScaledXp(int baseXp, int completionCount) {
        double multiplier = calculateProgressMultiplier(completionCount);

        return (int) Math.floor(baseXp * multiplier);
    }

    private int calculateScaledBits(int baseBits, int completionCount) {
        double multiplier = calculateProgressMultiplier(completionCount);

        return (int) Math.floor(baseBits * multiplier);
    }

    private List<RewardResponse> applyFixedRewards(
            UUID digimonId,
            MissionDefinition mission,
            int completionCount
    ) {
        double multiplier = calculateProgressMultiplier(completionCount);

        List<RewardResponse> rewards = new ArrayList<>();

        for (MissionReward reward : mission.getFixedRewards()) {

            int quantity = (int) Math.floor(reward.getBaseQuantity() * multiplier);

            if (quantity > 0) {
                addItemUseCase.execute(
                        digimonId,
                        reward.getItemType(),
                        quantity
                );

                rewards.add(
                        new RewardResponse(
                                reward.getItemType(),
                                quantity
                        )
                );
            }
        }

        return rewards;
    }

    private void applyRandomLoot(
            UUID digimonId,
            MissionDefinition mission,
            List<RewardResponse> rewards
    ) {
        if (mission.getLootTable() == null) {
            return;
        }

        LootItem lootItem = LootRoller.roll(mission.getLootTable());

        addItemUseCase.execute(
                digimonId,
                lootItem.getItemType(),
                lootItem.getQuantity()
        );

        rewards.add(
                new RewardResponse(
                        lootItem.getItemType(),
                        lootItem.getQuantity()
                )
        );
    }

    private double calculateProgressMultiplier(int completionCount) {
        return 1 + (completionCount * 0.01);
    }

    private void incrementProgress(PlayerMissionProgress progress) {
        progress.setCompletionCount(progress.getCompletionCount() + 1);
        progressRepository.save(progress);
    }
}