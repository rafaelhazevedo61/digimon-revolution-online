package com.dro.modules.evolution.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.api.dto.response.*;
import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.domain.EvolutionStepMaterial;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Evolução.
 */
@Service
@RequiredArgsConstructor
public class GetEvolutionOptionsUseCase {

    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EvolutionLineRepository evolutionLineRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final PlayerRepository playerRepository;

    public EvolutionOptionsResponse execute(String token, UUID digimonId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("This Digimon does not belong to the player");
        }

        if (digimon.getDigimonInfoId() == null) {
            throw new BadRequestException("Digimon has no linked DigimonInfo. Cannot determine evolution options.");
        }

        List<EvolutionLine> lines = evolutionLineRepository
                .findByActiveTrueAndSteps_DigimonInfo_Id(digimon.getDigimonInfoId());

        List<EvolutionOptionResponse> options = new ArrayList<>();

        for (EvolutionLine line : lines) {
            findNextStep(line, digimon).ifPresent(nextStep -> {
                EvolutionOptionResponse option = buildOption(line, nextStep, digimon);
                options.add(option);
            });
        }

        DigimonInfos currentInfo = digimonInfosRepository.findById(digimon.getDigimonInfoId())
                .orElse(null);

        return new EvolutionOptionsResponse(
                digimon.getDigimonInfoId(),
                digimon.getName(),
                digimon.getStage().name(),
                digimon.getLevel(),
                currentInfo != null ? currentInfo.getAttribute().name() : null,
                currentInfo != null ? currentInfo.getElement().name() : null,
                options
        );
    }

    private Optional<EvolutionLineStep> findNextStep(EvolutionLine line, Digimon digimon) {
        List<EvolutionLineStep> steps = line.getSteps().stream()
                .sorted(Comparator.comparingInt(EvolutionLineStep::getStepOrder))
                .toList();

        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).getDigimonInfo().getId().equals(digimon.getDigimonInfoId())) {
                return Optional.of(steps.get(i + 1));
            }
        }

        return Optional.empty();
    }

    private EvolutionOptionResponse buildOption(
            EvolutionLine line,
            EvolutionLineStep nextStep,
            Digimon digimon
    ) {
        DigimonInfos nextInfo = nextStep.getDigimonInfo();

        EvolutionNextStepResponse nextStepResponse = new EvolutionNextStepResponse(
                nextInfo.getId(),
                nextInfo.getName(),
                nextStep.getStage().name(),
                nextInfo.getAttribute().name(),
                nextInfo.getElement().name(),
                nextInfo.getSpecie().name(),
                nextInfo.getBaseHp(),
                nextInfo.getBaseAtk(),
                nextInfo.getBaseDef()
        );

        List<EvolutionMaterialRequirementResponse> materialResponses = new ArrayList<>();
        boolean hasMaterials = true;

        for (EvolutionStepMaterial material : nextStep.getMaterials()) {
            int playerHas = itemDefinitionRepository.findByCode(material.getMaterialCode())
                    .flatMap(itemDef -> inventoryRepository
                            .findByDigimonIdAndItemDefinitionId(digimon.getId(), itemDef.getId()))
                    .map(InventoryItem::getQuantity)
                    .orElse(0);

            materialResponses.add(new EvolutionMaterialRequirementResponse(
                    material.getMaterialCode(),
                    material.getDescription(),
                    material.getQuantity(),
                    playerHas
            ));

            if (playerHas < material.getQuantity()) {
                hasMaterials = false;
            }
        }

        EvolutionRequirementsResponse requirements = new EvolutionRequirementsResponse(
                nextStep.getRequiredLevel(),
                materialResponses
        );

        boolean levelMet = digimon.getLevel() >= nextStep.getRequiredLevel();
        boolean canEvolve = levelMet && hasMaterials;

        String reason = null;
        if (!levelMet) {
            reason = "Level insuficiente. Necessário: " + nextStep.getRequiredLevel();
        } else if (!hasMaterials) {
            reason = "Materiais insuficientes";
        }

        return new EvolutionOptionResponse(
                line.getId(),
                line.getCode(),
                line.getName(),
                nextStepResponse,
                requirements,
                canEvolve,
                reason
        );
    }
}
