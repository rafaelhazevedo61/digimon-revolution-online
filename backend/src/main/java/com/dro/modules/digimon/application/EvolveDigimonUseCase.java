package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.domain.EvolutionStepMaterial;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.application.ConsumeItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EvolveDigimonUseCase {

    private static final double HP_IV_WEIGHT = 0.30;
    private static final double ATTACK_IV_WEIGHT = 0.20;
    private static final double DEFENSE_IV_WEIGHT = 0.20;

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EvolutionLineRepository evolutionLineRepository;
    private final ConsumeItemUseCase consumeItemUseCase;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final TutorialService tutorialService;

    @Transactional
    public void execute(String token, Long evolutionLineId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository
                .findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (digimon.getDigimonInfoId() == null) {
            throw new BadRequestException("Digimon has no linked DigimonInfo. Cannot evolve.");
        }

        EvolutionLine line = resolveEvolutionLine(digimon, evolutionLineId);
        EvolutionLineStep nextStep = findNextStep(line, digimon);

        validateLevel(digimon, nextStep);
        consumeRequiredMaterials(digimon.getId(), nextStep);

        DigimonInfos nextInfo = nextStep.getDigimonInfo();

        DigimonInfos currentInfo = digimonInfosRepository.findById(digimon.getDigimonInfoId())
                .orElse(null);
        boolean hasCustomName = currentInfo == null
                || !digimon.getName().equals(currentInfo.getName());

        if (!hasCustomName) {
            digimon.setName(nextInfo.getName());
        }

        digimon.setStage(nextStep.getStage());
        digimon.setDigimonInfoId(nextInfo.getId());

        recalculateStats(digimon, nextInfo);

        digimonRepository.save(digimon);

        tutorialService.completeStep(playerId, TutorialStep.EVOLVE_DIGIMON);
    }

    private EvolutionLine resolveEvolutionLine(Digimon digimon, Long evolutionLineId) {

        if (evolutionLineId != null) {
            EvolutionLine line = evolutionLineRepository.findByIdAndActiveTrue(evolutionLineId)
                    .orElseThrow(() -> new NotFoundException("Evolution line not found or inactive"));

            boolean digimonInLine = line.getSteps().stream()
                    .anyMatch(step -> step.getDigimonInfo().getId().equals(digimon.getDigimonInfoId()));

            if (!digimonInLine) {
                throw new BadRequestException("Digimon does not belong to the specified evolution line");
            }

            return line;
        }

        List<EvolutionLine> lines = evolutionLineRepository
                .findByActiveTrueAndSteps_DigimonInfo_Id(digimon.getDigimonInfoId());

        List<EvolutionLine> linesWithNextStep = lines.stream()
                .filter(line -> hasNextStep(line, digimon))
                .toList();

        if (linesWithNextStep.isEmpty()) {
            throw new BadRequestException("No evolution line found for this Digimon");
        }

        if (linesWithNextStep.size() > 1) {
            throw new BadRequestException(
                    "Multiple evolution lines available. Please specify evolutionLineId. Options: "
                            + linesWithNextStep.stream()
                            .map(l -> l.getId() + " (" + l.getCode() + ")")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        return linesWithNextStep.get(0);
    }

    private boolean hasNextStep(EvolutionLine line, Digimon digimon) {
        List<EvolutionLineStep> steps = line.getSteps().stream()
                .sorted(Comparator.comparingInt(EvolutionLineStep::getStepOrder))
                .toList();

        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).getDigimonInfo().getId().equals(digimon.getDigimonInfoId())) {
                return true;
            }
        }
        return false;
    }

    private EvolutionLineStep findNextStep(EvolutionLine line, Digimon digimon) {
        List<EvolutionLineStep> steps = line.getSteps().stream()
                .sorted(Comparator.comparingInt(EvolutionLineStep::getStepOrder))
                .toList();

        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).getDigimonInfo().getId().equals(digimon.getDigimonInfoId())) {
                return steps.get(i + 1);
            }
        }

        throw new BadRequestException("Digimon cannot evolve further in this line");
    }

    private void validateLevel(Digimon digimon, EvolutionLineStep nextStep) {
        if (digimon.getLevel() < nextStep.getRequiredLevel()) {
            throw new BadRequestException(
                    "Level too low. Required: " + nextStep.getRequiredLevel());
        }
    }

    private void consumeRequiredMaterials(UUID digimonId, EvolutionLineStep nextStep) {
        for (EvolutionStepMaterial material : nextStep.getMaterials()) {
            ItemDefinition itemDef = itemDefinitionRepository
                    .findByCode(material.getMaterialCode())
                    .orElseThrow(() -> new NotFoundException(
                            "Item definition not found for material: " + material.getMaterialCode()));

            consumeItemUseCase.consumeMaterial(
                    digimonId,
                    itemDef.getId(),
                    material.getQuantity()
            );
        }
    }

    private void recalculateStats(Digimon digimon, DigimonInfos digimonInfo) {

        double rarityMultiplier =
                RarityRules.getStatMultiplier(digimon.getRarity());

        double stageMultiplier =
                EvolutionRules.stageStatMultiplier(digimon.getStage());

        double rebirthMultiplier =
                RebirthRules.calculateStatMultiplier(digimon.getRebirthCount());

        double hpMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(digimon.getPersonality())
                        * TraitRules.getHpMultiplier(digimon.getTrait())
                        * rebirthMultiplier;

        double attackMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(digimon.getPersonality())
                        * TraitRules.getAttackMultiplier(digimon.getTrait())
                        * rebirthMultiplier;

        double defenseMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(digimon.getPersonality())
                        * TraitRules.getDefenseMultiplier(digimon.getTrait())
                        * rebirthMultiplier;

        digimon.setHp((int) Math.floor(
                (digimonInfo.getBaseHp() + digimon.getIvHp() * HP_IV_WEIGHT) * hpMultiplier
        ));

        digimon.setAttack((int) Math.floor(
                (digimonInfo.getBaseAtk() + digimon.getIvAttack() * ATTACK_IV_WEIGHT) * attackMultiplier
        ));

        digimon.setDefense((int) Math.floor(
                (digimonInfo.getBaseDef() + digimon.getIvDefense() * DEFENSE_IV_WEIGHT) * defenseMultiplier
        ));
    }
}
