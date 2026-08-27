package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class RebirthUseCase {
    private static final int MAX_IV = 100;
    private static final int REQUIRED_LEVEL = 100;
    private static final double HP_IV_WEIGHT = 0.3;
    private static final double ATTACK_IV_WEIGHT = 0.2;
    private static final double DEFENSE_IV_WEIGHT = 0.2;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EvolutionLineRepository evolutionLineRepository;
    private final ClanMissionProgressTracker clanMissionProgressTracker;
    private final Random random = new Random();

    @Transactional
    public void execute(String token, UUID digimonId) {
        execute(token, digimonId, 0, 0, 0);
    }

    public void execute(String token, UUID digimonId, int codeInfiniteHp, int codeInfiniteAttack, int codeInfiniteDefense) {
        UUID playerId = extractPlayerId(token);
        Player player = findPlayer(playerId);
        Digimon oldDigimon = findDigimon(digimonId);
        validateOwner(oldDigimon, playerId);
        validateStatus(oldDigimon);
        validateLevel(oldDigimon);
        validateStage(oldDigimon);
        validateActiveMission(oldDigimon);
        int currentRebirthCount = oldDigimon.getRebirthCount();
        int newRebirthCount = currentRebirthCount + 1;
        int bitsCost = RebirthRules.calculateBitsCost(currentRebirthCount);
        int dataCoreCost = RebirthRules.calculateDataCoreCost(currentRebirthCount);
        int digitalDataCost = RebirthRules.calculateDigitalDataCost(currentRebirthCount);
        validateCodeInfiniteInvestment(codeInfiniteHp, codeInfiniteAttack, codeInfiniteDefense);
        int codeInfiniteCost = codeInfiniteHp + codeInfiniteAttack + codeInfiniteDefense;
        validateBits(oldDigimon, bitsCost);
        validateDigitalData(player, digitalDataCost);
        InventoryItem dataCore = findDataCore(digimonId);
        validateDataCore(dataCore, dataCoreCost);
        InventoryItem codeInfinite = findCodeInfinite(digimonId, codeInfiniteCost);
        validateCodeInfinite(codeInfinite, codeInfiniteCost);
        consumeCosts(player, oldDigimon, dataCore, codeInfinite, bitsCost, dataCoreCost, digitalDataCost, codeInfiniteCost);
        Digimon newDigimon = createRebornDigimon(playerId, oldDigimon, newRebirthCount, codeInfiniteHp, codeInfiniteAttack, codeInfiniteDefense);
        oldDigimon.setStatus(DigimonStatus.REBORN);
        oldDigimon.setBits(0);
        digimonRepository.save(oldDigimon);
        digimonRepository.save(newDigimon);
        inventoryRepository.save(dataCore);
        if (codeInfinite != null && codeInfiniteCost > 0) inventoryRepository.save(codeInfinite);
        if (player.getClanId() != null) {
            clanMissionProgressTracker.track(playerId, ClanMissionObjectiveType.REBIRTHS_DONE);
        }
        updateActiveDigimonIfNeeded(player, oldDigimon, newDigimon);
    }

    private UUID extractPlayerId(String token) {
        return TokenExtractor.extractPlayerId(token);
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
    }

    private Digimon findDigimon(UUID digimonId) {
        return digimonRepository.findByIdForUpdate(digimonId).orElseThrow(() -> new NotFoundException("Digimon not found"));
    }

    private void validateOwner(Digimon digimon, UUID playerId) {
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("This Digimon does not belong to the player");
        }
    }

    private void validateStatus(Digimon digimon) {
        if (digimon.getStatus() != DigimonStatus.ACTIVE) {
            throw new BadRequestException("Only active Digimons can perform Rebirth");
        }
    }

    private void validateLevel(Digimon digimon) {
        if (digimon.getLevel() < REQUIRED_LEVEL) {
            throw new BadRequestException("Digimon must be level 100 to perform Rebirth");
        }
    }

    private void validateStage(Digimon digimon) {
        if (!RebirthRules.isEligibleStage(digimon.getStage())) {
            throw new BadRequestException("Digimon must be Champion or higher to perform Rebirth");
        }
    }

    private void validateActiveMission(Digimon digimon) {
        boolean hasRunningMission = missionInstanceRepository.existsByDigimonIdAndStatus(digimon.getId(), MissionStatus.RUNNING);
        if (hasRunningMission) {
            throw new ConflictException("Digimon cannot perform Rebirth while in a running mission");
        }
    }

    private void validateBits(Digimon digimon, int bitsCost) {
        if (digimon.getBits() < bitsCost) {
            throw new UnprocessableException("Not enough Bits to perform Rebirth");
        }
    }

    private InventoryItem findDataCore(UUID digimonId) {
        return inventoryRepository.findByDigimonIdAndItemType(digimonId, ItemType.DATA_CORE).orElseThrow(() -> new NotFoundException("Data Core not found in inventory"));
    }

    private void validateDigitalData(Player player, int digitalDataCost) {
        if (player.getDigitalData() < digitalDataCost) {
            throw new UnprocessableException("Not enough Digital Data to perform Rebirth");
        }
    }

    private void validateDataCore(InventoryItem dataCore, int dataCoreCost) {
        if (dataCore.getQuantity() < dataCoreCost) {
            throw new UnprocessableException("Not enough Data Core to perform Rebirth");
        }
    }

    private InventoryItem findCodeInfinite(UUID digimonId, int required) {
        return inventoryRepository.findByDigimonIdAndItemType(digimonId, ItemType.CODE_INFINITE)
                .orElseGet(() -> required == 0 ? null : null);
    }

    private void validateCodeInfiniteInvestment(int hp, int attack, int defense) {
        if (hp < 0 || attack < 0 || defense < 0 || hp + attack + defense > RebirthRules.calculateMaxCodeInfiniteInvestment()) {
            throw new BadRequestException("Code Infinite investment must be non-negative and total at most 100");
        }
    }

    private void validateCodeInfinite(InventoryItem codeInfinite, int cost) {
        if (cost > 0 && (codeInfinite == null || codeInfinite.getQuantity() < cost)) {
            throw new UnprocessableException("Not enough Code Infinite to refine Rebirth");
        }
    }

    private void consumeCosts(Player player, Digimon digimon, InventoryItem dataCore, InventoryItem codeInfinite, int bitsCost, int dataCoreCost, int digitalDataCost, int codeInfiniteCost) {
        digimon.setBits(digimon.getBits() - bitsCost);
        dataCore.setQuantity(dataCore.getQuantity() - dataCoreCost);
        if (codeInfinite != null) codeInfinite.setQuantity(codeInfinite.getQuantity() - codeInfiniteCost);
        if (!player.spendDigitalData(digitalDataCost)) {
            throw new UnprocessableException("Not enough Digital Data to perform Rebirth");
        }
        playerRepository.save(player);
    }

    private Digimon createRebornDigimon(UUID playerId, Digimon oldDigimon, int newRebirthCount, int codeInfiniteHp, int codeInfiniteAttack, int codeInfiniteDefense) {
        Rarity rarity = RarityRoller.rollForRebirth(oldDigimon.getRarity(), newRebirthCount);
        Personality personality = PersonalityRoller.roll();
        Trait trait = TraitRoller.rollForRebirth(newRebirthCount);
        int rarityMinimumIv = RarityRules.getMinimumIv(rarity);
        int ivHp = rollInheritedIv(oldDigimon.getIvHp(), rarityMinimumIv, newRebirthCount, codeInfiniteHp);
        int ivAttack = rollInheritedIv(oldDigimon.getIvAttack(), rarityMinimumIv, newRebirthCount, codeInfiniteAttack);
        int ivDefense = rollInheritedIv(oldDigimon.getIvDefense(), rarityMinimumIv, newRebirthCount, codeInfiniteDefense);
        DigimonGrade grade = DigimonGradeRules.calculate(ivHp, ivAttack, ivDefense);
        Long babyInfoId = resolveBabyDigimonInfoId(oldDigimon);
        DigimonInfos babyInfo = babyInfoId != null ? digimonInfosRepository.findById(babyInfoId).orElse(null) : null;
        int baseHp = 10;
        int baseAtk = 5;
        int baseDef = 3;
        if (babyInfo != null) {
            baseHp = babyInfo.getBaseHp();
            baseAtk = babyInfo.getBaseAtk();
            baseDef = babyInfo.getBaseDef();
        }
        double rarityMultiplier = RarityRules.getStatMultiplier(rarity);
        double stageMultiplier = EvolutionRules.stageStatMultiplier(Stage.BABY);
        double rebirthMultiplier = RebirthRules.calculateStatMultiplier(newRebirthCount);
        int hp = (int) Math.floor((baseHp + (ivHp * HP_IV_WEIGHT)) * rarityMultiplier * stageMultiplier * PersonalityRules.getHpMultiplier(personality) * TraitRules.getHpMultiplier(trait) * rebirthMultiplier);
        int attack = (int) Math.floor((baseAtk + (ivAttack * ATTACK_IV_WEIGHT)) * rarityMultiplier * stageMultiplier * PersonalityRules.getAttackMultiplier(personality) * TraitRules.getAttackMultiplier(trait) * rebirthMultiplier);
        int defense = (int) Math.floor((baseDef + (ivDefense * DEFENSE_IV_WEIGHT)) * rarityMultiplier * stageMultiplier * PersonalityRules.getDefenseMultiplier(personality) * TraitRules.getDefenseMultiplier(trait) * rebirthMultiplier);
        int maxEnergy = 20 + TraitRules.getMaxEnergyBonus(trait);
        String rebornName = babyInfo != null ? babyInfo.getName() : "Reborn " + oldDigimon.getType();
        return Digimon.builder().id(UUID.randomUUID()).playerId(playerId).name(rebornName).type(oldDigimon.getType()).stage(Stage.BABY).digimonInfoId(babyInfoId).level(1).experience(0).hp(hp).attack(attack).defense(defense).ivHp(ivHp).ivAttack(ivAttack).ivDefense(ivDefense).grade(grade).rarity(rarity).personality(personality).energy(maxEnergy).maxEnergy(maxEnergy).trait(trait).lastEnergyUpdate(Instant.now()).createdAt(LocalDateTime.now()).bits(oldDigimon.getBits()).rebirthCount(newRebirthCount).rebornedFrom(oldDigimon.getId()).status(DigimonStatus.ACTIVE).build();
    }

    private Long resolveBabyDigimonInfoId(Digimon oldDigimon) {
        Long infoId = oldDigimon.getDigimonInfoId();
        // Fallback for legacy digimon: resolve infoId from current name
        if (infoId == null) {
            infoId = digimonInfosRepository.findByName(oldDigimon.getName()).map(DigimonInfos::getId).orElse(null);
        }
        if (infoId == null) {
            return null;
        }
        // Find the evolution line and return the first (baby) step's DigimonInfos ID
        List<EvolutionLine> lines = evolutionLineRepository.findByActiveTrueAndSteps_DigimonInfo_Id(infoId);
        if (!lines.isEmpty()) {
            return lines.get(0).getSteps().stream().min(java.util.Comparator.comparingInt(EvolutionLineStep::getStepOrder)).map(step -> step.getDigimonInfo().getId()).orElse(null);
        }
        return null;
    }

    private int rollInheritedIv(int previousIv, int rarityMinimumIv, int rebirthCount, int codeInfiniteAmount) {
        int inheritedMinimum = RebirthRules.calculateInheritedIvMinimum(previousIv, rarityMinimumIv, rebirthCount);
        inheritedMinimum = Math.min(MAX_IV, inheritedMinimum + RebirthRules.calculateCodeInfiniteIvBonus(codeInfiniteAmount));
        return inheritedMinimum + random.nextInt((MAX_IV - inheritedMinimum) + 1);
    }

    private void updateActiveDigimonIfNeeded(Player player, Digimon oldDigimon, Digimon newDigimon) {
        if (oldDigimon.getId().equals(player.getActiveDigimonId())) {
            player.setActiveDigimonId(newDigimon.getId());
            playerRepository.save(player);
        }
    }

    public RebirthUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final InventoryRepository inventoryRepository, final MissionInstanceRepository missionInstanceRepository, final DigimonInfosRepository digimonInfosRepository, final EvolutionLineRepository evolutionLineRepository, final ClanMissionProgressTracker clanMissionProgressTracker) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.inventoryRepository = inventoryRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.evolutionLineRepository = evolutionLineRepository;
        this.clanMissionProgressTracker = clanMissionProgressTracker;
    }
}
