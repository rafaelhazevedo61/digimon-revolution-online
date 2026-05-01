package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.ConsumeItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvolveDigimonUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ConsumeItemUseCase consumeItemUseCase;

    public void execute (String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository
                .findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        Stage currentStage = digimon.getStage();
        Stage nextStage = EvolutionRules.nextStage(currentStage);

        if (nextStage == null) {
            throw new BadRequestException("Digimon cannot evolve further");
        }

        validateLevelRequirement(digimon, currentStage);
        consumeRequiredFragments(digimon.getId(), currentStage);

        digimon.setStage(nextStage);

        recalculateStats(digimon);

        digimonRepository.save(digimon);
    }

    private void validateLevelRequirement (
            Digimon digimon,
            Stage currentStage
    ) {
        int requiredLevel = EvolutionRules.requiredLevel(currentStage);

        if (digimon.getLevel() < requiredLevel) {
            throw new BadRequestException(
                    "Level too low. Required: " + requiredLevel
            );
        }
    }

    private void consumeRequiredFragments (
            UUID digimonId,
            Stage currentStage
    ) {
        ItemType fragment = EvolutionRules.requiredFragment(currentStage);
        int quantity = EvolutionRules.requiredFragmentQuantity(currentStage);

        if (fragment == null || quantity == 0) {
            return; // BABY → ROOKIE não precisa fragmento
        }

        consumeItemUseCase.execute(digimonId, fragment, quantity);
    }

    private void recalculateStats (Digimon digimon) {

        System.out.println("Digimon status anteriores: HP=" + digimon.getHp() + ", ATK=" + digimon.getAttack() + ", DEF=" + digimon.getDefense());

        double rarityMultiplier =
                RarityRules.getStatMultiplier(digimon.getRarity());

        double stageMultiplier =
                EvolutionRules.stageStatMultiplier(digimon.getStage());

        double hpMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(digimon.getPersonality());

        double attackMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(digimon.getPersonality());

        double defenseMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(digimon.getPersonality());

        int newHp = (int) Math.floor(
                (10 + digimon.getIvHp()) * hpMultiplier
        );

        int newAttack = (int) Math.floor(
                (5 + digimon.getIvAttack()) * attackMultiplier
        );

        int newDefense = (int) Math.floor(
                (5 + digimon.getIvDefense()) * defenseMultiplier
        );

        System.out.println("Digimon status recalculados: HP=" + newHp + ", ATK=" + newAttack + ", DEF=" + newDefense);

        digimon.setHp(newHp);
        digimon.setAttack(newAttack);
        digimon.setDefense(newDefense);
    }
}
