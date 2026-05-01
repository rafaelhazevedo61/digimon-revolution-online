package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RebirthUseCase {

    private static final int MAX_IV = 100;
    private static final int REQUIRED_LEVEL = 100;

    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;

    private final Random random = new Random();

    @Transactional
    public void execute(String token, UUID digimonId) {

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

        validateBits(oldDigimon, bitsCost);

        InventoryItem dataCore = findDataCore(digimonId);
        validateDataCore(dataCore, dataCoreCost);

        consumeCosts(oldDigimon, dataCore, bitsCost, dataCoreCost);

        Digimon newDigimon = createRebornDigimon(playerId, oldDigimon, newRebirthCount);

        oldDigimon.setStatus(DigimonStatus.REBORN);
        oldDigimon.setBits(0);

        digimonRepository.save(oldDigimon);
        digimonRepository.save(newDigimon);
        inventoryRepository.save(dataCore);

        updateActiveDigimonIfNeeded(player, oldDigimon, newDigimon);
    }

    private UUID extractPlayerId(String token) {
        return UUID.fromString(token.split(":")[1]);
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    private Digimon findDigimon(UUID digimonId) {
        return digimonRepository.findById(digimonId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));
    }

    private void validateOwner(Digimon digimon, UUID playerId) {
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("This Digimon does not belong to the player");
        }
    }

    private void validateStatus(Digimon digimon) {
        if (digimon.getStatus() != DigimonStatus.ACTIVE) {
            throw new RuntimeException("Only active Digimons can perform Rebirth");
        }
    }

    private void validateLevel(Digimon digimon) {
        if (digimon.getLevel() < REQUIRED_LEVEL) {
            throw new RuntimeException("Digimon must be level 100 to perform Rebirth");
        }
    }

    private void validateStage(Digimon digimon) {
        if (!RebirthRules.isEligibleStage(digimon.getStage())) {
            throw new RuntimeException("Digimon must be Champion or higher to perform Rebirth");
        }
    }

    private void validateActiveMission(Digimon digimon) {
        boolean hasRunningMission = missionInstanceRepository.existsByDigimonIdAndStatus(
                digimon.getId(),
                MissionStatus.RUNNING
        );

        if (hasRunningMission) {
            throw new RuntimeException("Digimon cannot perform Rebirth while in a running mission");
        }
    }

    private void validateBits(Digimon digimon, int bitsCost) {
        if (digimon.getBits() < bitsCost) {
            throw new RuntimeException("Not enough Bits to perform Rebirth");
        }
    }

    private InventoryItem findDataCore(UUID digimonId) {
        return inventoryRepository.findByDigimonIdAndItemType(digimonId, ItemType.DATA_CORE)
                .orElseThrow(() -> new RuntimeException("Data Core not found in inventory"));
    }

    private void validateDataCore(InventoryItem dataCore, int dataCoreCost) {
        if (dataCore.getQuantity() < dataCoreCost) {
            throw new RuntimeException("Not enough Data Core to perform Rebirth");
        }
    }

    private void consumeCosts(
            Digimon digimon,
            InventoryItem dataCore,
            int bitsCost,
            int dataCoreCost
    ) {
        digimon.setBits(digimon.getBits() - bitsCost);
        dataCore.setQuantity(dataCore.getQuantity() - dataCoreCost);
    }

    private Digimon createRebornDigimon(
            UUID playerId,
            Digimon oldDigimon,
            int newRebirthCount
    ) {

        Rarity rarity = RarityRoller.rollForRebirth(
                oldDigimon.getRarity(),
                newRebirthCount
        );

        Personality personality = PersonalityRoller.roll();

        Trait trait = TraitRoller.rollForRebirth(newRebirthCount);

        int rarityMinimumIv = RarityRules.getMinimumIv(rarity);

        int ivHp = rollInheritedIv(
                oldDigimon.getIvHp(),
                rarityMinimumIv,
                newRebirthCount
        );

        int ivAttack = rollInheritedIv(
                oldDigimon.getIvAttack(),
                rarityMinimumIv,
                newRebirthCount
        );

        int ivDefense = rollInheritedIv(
                oldDigimon.getIvDefense(),
                rarityMinimumIv,
                newRebirthCount
        );

        double rarityMultiplier = RarityRules.getStatMultiplier(rarity);
        double stageMultiplier = EvolutionRules.stageStatMultiplier(Stage.BABY);
        double rebirthMultiplier = RebirthRules.calculateStatMultiplier(newRebirthCount);

        int hp = (int) Math.floor(
                (10 + ivHp)
                        * rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(personality)
                        * TraitRules.getHpMultiplier(trait)
                        * rebirthMultiplier
        );

        int attack = (int) Math.floor(
                (5 + ivAttack)
                        * rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(personality)
                        * TraitRules.getAttackMultiplier(trait)
                        * rebirthMultiplier
        );

        int defense = (int) Math.floor(
                (5 + ivDefense)
                        * rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(personality)
                        * TraitRules.getDefenseMultiplier(trait)
                        * rebirthMultiplier
        );

        int maxEnergy = 20 + TraitRules.getMaxEnergyBonus(trait);

        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Reborn " + oldDigimon.getType())
                .type(oldDigimon.getType())
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .hp(hp)
                .attack(attack)
                .defense(defense)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .rarity(rarity)
                .personality(personality)
                .energy(maxEnergy)
                .maxEnergy(maxEnergy)
                .trait(trait)
                .lastEnergyUpdate(Instant.now())
                .createdAt(LocalDateTime.now())
                .bits(oldDigimon.getBits())
                .rebirthCount(newRebirthCount)
                .rebornedFrom(oldDigimon.getId())
                .status(DigimonStatus.ACTIVE)
                .build();
    }

    private int rollInheritedIv(
            int previousIv,
            int rarityMinimumIv,
            int rebirthCount
    ) {
        int inheritedMinimum = RebirthRules.calculateInheritedIvMinimum(
                previousIv,
                rarityMinimumIv,
                rebirthCount
        );

        return inheritedMinimum + random.nextInt((MAX_IV - inheritedMinimum) + 1);
    }

    private void updateActiveDigimonIfNeeded(
            Player player,
            Digimon oldDigimon,
            Digimon newDigimon
    ) {
        if (oldDigimon.getId().equals(player.getActiveDigimonId())) {
            player.setActiveDigimonId(newDigimon.getId());
            playerRepository.save(player);
        }
    }
}