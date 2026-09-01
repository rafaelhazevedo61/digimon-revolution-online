package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootItem;
import com.dro.modules.loot.domain.LootRoller;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mission.api.dto.response.MissionDigimonExperienceResponse;
import com.dro.modules.mission.api.dto.response.MissionResultResponse;
import com.dro.modules.mission.api.dto.response.RewardResponse;
import com.dro.modules.mission.domain.MissionDefinition;
import com.dro.modules.mission.domain.MissionDefinitionMapper;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionReward;
import com.dro.modules.mission.domain.MissionProgressionRules;
import com.dro.modules.mission.domain.PlayerMissionProgress;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.PlayerMissionProgressRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.activitycalendar.application.ActivityCalendarService;
import com.dro.modules.activitycalendar.domain.ActivitySource;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.domain.RarityRules;
import com.dro.modules.digimon.domain.PersonalityRules;
import com.dro.modules.digimon.domain.TraitRules;
import com.dro.modules.mission.api.dto.response.MissionRewardBreakdownResponse;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedContentResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.util.TokenExtractor;
import com.dro.shared.gameplay.WeekendDoubleRewardRules;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class ClaimMissionUseCase {

    private final MissionInstanceRepository missionInstanceRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final PlayerMissionProgressRepository progressRepository;
    private final AddItemUseCase addItemUseCase;
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final TutorialService tutorialService;
    private final ClanBonusService clanBonusService;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final PlayerRepository playerRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final TransactionAuditPublisher transactionAuditPublisher;
    private final ActivityCalendarService activityCalendarService;
    private final NewlyUnlockedContentService newlyUnlockedContentService;

    public ClaimMissionUseCase(
            MissionInstanceRepository missionInstanceRepository,
            DigimonRepository digimonRepository,
            DigimonInfosRepository digimonInfosRepository,
            PlayerMissionProgressRepository progressRepository,
            AddItemUseCase addItemUseCase,
            MissionDefinitionRepository missionDefinitionRepository,
            TutorialService tutorialService,
            ClanBonusService clanBonusService,
            ClanMissionProgressTracker clanMissionProgressTracker,
            PlayerRepository playerRepository,
            ChestDefinitionRepository chestDefinitionRepository,
            ItemDefinitionRepository itemDefinitionRepository,
            TransactionAuditPublisher transactionAuditPublisher,
            ActivityCalendarService activityCalendarService
    ) {
        this(missionInstanceRepository, digimonRepository, digimonInfosRepository, progressRepository, addItemUseCase, missionDefinitionRepository,
                tutorialService, clanBonusService, clanMissionProgressTracker, playerRepository, chestDefinitionRepository,
                itemDefinitionRepository, transactionAuditPublisher, activityCalendarService, null);
    }

    @Autowired
    public ClaimMissionUseCase(
            MissionInstanceRepository missionInstanceRepository,
            DigimonRepository digimonRepository,
            DigimonInfosRepository digimonInfosRepository,
            PlayerMissionProgressRepository progressRepository,
            AddItemUseCase addItemUseCase,
            MissionDefinitionRepository missionDefinitionRepository,
            TutorialService tutorialService,
            ClanBonusService clanBonusService,
            ClanMissionProgressTracker clanMissionProgressTracker,
            PlayerRepository playerRepository,
            ChestDefinitionRepository chestDefinitionRepository,
            ItemDefinitionRepository itemDefinitionRepository,
            TransactionAuditPublisher transactionAuditPublisher,
            ActivityCalendarService activityCalendarService,
            NewlyUnlockedContentService newlyUnlockedContentService
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.digimonRepository = digimonRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.progressRepository = progressRepository;
        this.addItemUseCase = addItemUseCase;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.tutorialService = tutorialService;
        this.clanBonusService = clanBonusService;
        this.clanMissionProgressTracker = clanMissionProgressTracker;
        this.playerRepository = playerRepository;
        this.chestDefinitionRepository = chestDefinitionRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.transactionAuditPublisher = transactionAuditPublisher;
        this.activityCalendarService = activityCalendarService;
        this.newlyUnlockedContentService = newlyUnlockedContentService;
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

        List<UUID> digimonIds = instance.getDigimonIds();
        List<Digimon> digimons;
        if (digimonIds.size() == 1) {
            digimons = List.of(digimonRepository.findById(digimonIds.get(0))
                    .orElseThrow(() -> new NotFoundException("Digimon não encontrado")));
        } else {
            List<Digimon> loadedDigimons = digimonRepository.findAllByIdForUpdate(playerId, digimonIds);
            if (loadedDigimons.size() != digimonIds.size()) {
                throw new NotFoundException("Um ou mais Digimons da missão não foram encontrados");
            }
            digimons = digimonIds.stream()
                    .map(id -> loadedDigimons.stream()
                            .filter(candidate -> candidate.getId().equals(id))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Digimon não encontrado")))
                    .toList();
        }
        Digimon digimon = digimons.get(0);

        MissionDefinition mission = MissionDefinitionMapper.toDefinition(
                missionDefinitionRepository.findById(instance.getMissionId())
                        .orElseThrow(() -> new NotFoundException("Mission not found"))
        );

        PlayerMissionProgress progress =
                getOrCreateProgress(playerId, mission.getId());

        int completionCount = progress.getCompletionCount();

        int previousLevel = digimon.getLevel();
        Stage previousStage = digimon.getStage();

        Player player = playerRepository.findById(playerId)
                .orElse(null);
        UUID clanId = player != null ? player.getClanId() : null;

        double xpMultiplier = clanId != null ? clanBonusService.getMissionXpMultiplier(clanId) : 1.0;
        double bitsMultiplier = clanId != null ? clanBonusService.getMissionBitsMultiplier(clanId) : 1.0;

        Instant rewardTime = Instant.now();
        double missionProgressMultiplier = calculateProgressMultiplier(completionCount);
        int eventMultiplier = WeekendDoubleRewardRules.isActive(rewardTime)
                ? WeekendDoubleRewardRules.XP_MULTIPLIER
                : 1;

        int xpBeforeDigimonMultiplier = WeekendDoubleRewardRules.multiplyXp((int) Math.floor(
                calculateScaledXp(mission.getBaseXp(), completionCount) * xpMultiplier
        ), rewardTime);
        double digimonXpMultiplier = RarityRules.getXpMultiplier(digimon.getRarity())
                * PersonalityRules.getXpMultiplier(digimon.getPersonality())
                * TraitRules.getXpMultiplier(digimon.getTrait());
        int xpGained = 0;
        boolean levelUp = false;
        for (Digimon member : digimons) {
            int memberPreviousLevel = member.getLevel();
            xpGained += member.gainExperience(xpBeforeDigimonMultiplier);
            levelUp = levelUp || member.getLevel() > memberPreviousLevel;
        }

        int bitsBeforeEventMultiplier = (int) Math.floor(
                calculateScaledBits(mission.getBaseBits(), completionCount) * bitsMultiplier
        );
        int bitsGained = WeekendDoubleRewardRules.multiplyBits(bitsBeforeEventMultiplier, rewardTime);

        if (bitsGained > 0) {
            digimon.setBits(digimon.getBits() + bitsGained);
        }

        List<RewardResponse> rewards = new ArrayList<>();

        UUID digimonId = instance.getDigimonId();

        if (!hasMissionChest(mission)) {
            rewards.addAll(
                    applyFixedRewards(digimonId, mission, completionCount)
            );
        }

        applyMissionChestOrLegacyLoot(digimonId, mission, rewards);

        incrementProgress(progress);

        instance.markClaimed();

        missionInstanceRepository.save(instance);
        digimons.forEach(digimonRepository::save);
        if (activityCalendarService != null) activityCalendarService.recordActivity(playerId, ActivitySource.MISSION_COMPLETED, missionInstanceId.toString());
        NewlyUnlockedContentResponse newlyUnlockedContent = newlyUnlockedContentService == null
                ? NewlyUnlockedContentResponse.empty()
                : newlyUnlockedContentService.detect(digimon, previousLevel, previousStage);

        if (clanId != null) {
            clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.MISSIONS_COMPLETED);
        }

        tutorialService.completeStep(playerId, TutorialStep.COMPLETE_MISSION);

        transactionAuditPublisher.success(
                "mission-claim:" + missionInstanceId,
                "MISSION_CLAIMED",
                "MissionInstance",
                missionInstanceId.toString(),
                buildAuditPayload(playerId, mission, xpGained, bitsGained)
        );

        List<MissionDigimonExperienceResponse> digimonExperience = digimons.stream()
                .map(member -> MissionDigimonExperienceResponse.from(member, missionDigimonImageUrl(member)))
                .toList();

        return new MissionResultResponse(
                mission.getId(),
                instance.getTeamId(),
                digimonExperience,
                xpGained,
                bitsGained,
                levelUp,
                rewards,
                newlyUnlockedContent,
                new MissionRewardBreakdownResponse(
                        mission.getBaseXp(),
                        missionProgressMultiplier,
                        xpMultiplier,
                        eventMultiplier,
                        digimonXpMultiplier,
                        missionProgressMultiplier * xpMultiplier * eventMultiplier * digimonXpMultiplier,
                        effectiveMultiplier(mission.getBaseXp(), xpGained),
                        xpBeforeDigimonMultiplier,
                        xpGained
                ),
                new MissionRewardBreakdownResponse(
                        mission.getBaseBits(),
                        missionProgressMultiplier,
                        bitsMultiplier,
                        WeekendDoubleRewardRules.isActive(rewardTime)
                                ? WeekendDoubleRewardRules.BITS_MULTIPLIER
                                : 1,
                        1.0,
                        missionProgressMultiplier * bitsMultiplier
                                * (WeekendDoubleRewardRules.isActive(rewardTime)
                                ? WeekendDoubleRewardRules.BITS_MULTIPLIER
                                : 1),
                        effectiveMultiplier(mission.getBaseBits(), bitsGained),
                        bitsBeforeEventMultiplier,
                        bitsGained
                )
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

    private boolean hasMissionChest(MissionDefinition mission) {
        return mission.getChestCode() != null && !mission.getChestCode().isBlank();
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
                ItemDefinition itemDefinition = itemDefinitionRepository
                        .findByCode(reward.getItemType().name())
                        .orElse(null);

                if (itemDefinition != null) {
                    addItemUseCase.addMaterial(digimonId, itemDefinition, quantity);
                } else {
                    addItemUseCase.execute(
                            digimonId,
                            reward.getItemType(),
                            quantity
                    );
                }

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

    private void applyMissionChestOrLegacyLoot(
            UUID digimonId,
            MissionDefinition mission,
            List<RewardResponse> rewards
    ) {
        if (hasMissionChest(mission)) {
            ChestDefinitionEntity chest = chestDefinitionRepository
                    .findWithCatalogByCode(mission.getChestCode())
                    .orElseThrow(() -> new ConflictException(
                            "Baú da missão não encontrado ou inativo: " + mission.getChestCode()));

            addItemUseCase.addMaterial(digimonId, chest.getItemDefinition(), 1);
            rewards.add(new RewardResponse(
                    ItemType.LOOT_CHEST,
                    1,
                    chest.getCode(),
                    chest.getName()
            ));
            return;
        }

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

    private Map<String, Object> buildAuditPayload(
            UUID playerId,
            MissionDefinition mission,
            int xpGained,
            int bitsGained
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "mission");
        payload.put("operation", "claim");
        payload.put("playerId", playerId.toString());
        payload.put("missionId", mission.getId());
        payload.put("area", mission.getArea().name());
        payload.put("xpGained", xpGained);
        payload.put("bitsGained", bitsGained);
        if (mission.getChestCode() != null) {
            payload.put("chestCode", mission.getChestCode());
            payload.put("chestQuantity", 1);
        }
        payload.put("summary", "Mission claimed successfully");
        return payload;
    }

    private String missionDigimonImageUrl(Digimon digimon) {
        if (digimonInfosRepository == null || digimon.getDigimonInfoId() == null) return null;
        return digimonInfosRepository.findById(digimon.getDigimonInfoId())
                .map(DigimonInfos::getImageUrl)
                .orElse(null);
    }

    private double effectiveMultiplier(int baseAmount, int finalAmount) {
        return baseAmount > 0 ? finalAmount / (double) baseAmount : 0.0;
    }

    private double calculateProgressMultiplier(int completionCount) {
        return MissionProgressionRules.rewardMultiplier(completionCount);
    }

    private void incrementProgress(PlayerMissionProgress progress) {
        progress.setCompletionCount(
                MissionProgressionRules.nextCompletionCount(progress.getCompletionCount())
        );
        progressRepository.save(progress);
    }
}