package com.dro.modules.evolution.application;

import com.dro.modules.evolution.api.dto.response.EvolutionLinePageResponse;
import com.dro.modules.evolution.api.dto.response.EvolutionLineResponse;
import com.dro.modules.evolution.api.dto.response.EvolutionLineStepMaterialResponse;
import com.dro.modules.evolution.api.dto.response.EvolutionLineStepResponse;
import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.domain.EvolutionStepMaterial;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.evolution.infra.spec.EvolutionLineSpecifications;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Evolução.
 */
@Service
public class GetEvolutionLinesUseCase {
    private final EvolutionLineRepository evolutionLineRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    public EvolutionLinePageResponse execute(String code, String name, Boolean active, Pageable pageable) {
        Page<EvolutionLineResponse> lines = evolutionLineRepository.findAll(EvolutionLineSpecifications.withFilters(code, name, active), pageable).map(this::toResponse);
        return EvolutionLinePageResponse.from(lines);
    }

    private EvolutionLineResponse toResponse(EvolutionLine line) {
        List<EvolutionLineStepResponse> steps = line.getSteps().stream().sorted(Comparator.comparing(EvolutionLineStep::getStepOrder)).map(this::toStepResponse).toList();
        return new EvolutionLineResponse(line.getId(), line.getCode(), line.getName(), line.isActive(), steps);
    }

    private EvolutionLineStepResponse toStepResponse(EvolutionLineStep step) {
        List<EvolutionLineStepMaterialResponse> materials = step.getMaterials().stream().map(this::toMaterialResponse).toList();
        return new EvolutionLineStepResponse(step.getStepOrder(), step.getDigimonInfo().getId(), step.getDigimonInfo().getName(), step.getDigimonInfo().getStage().name(), step.getRequiredLevel(), materials);
    }

    private EvolutionLineStepMaterialResponse toMaterialResponse(EvolutionStepMaterial material) {
        ItemDefinition itemDefinition = itemDefinitionRepository.findByCode(material.getMaterialCode()).orElse(null);
        if (itemDefinition == null) {
            return new EvolutionLineStepMaterialResponse(null, material.getMaterialCode(), material.getDescription(), material.getQuantity());
        }
        return new EvolutionLineStepMaterialResponse(itemDefinition.getId(), itemDefinition.getCode(), itemDefinition.getName(), material.getQuantity());
    }

    public GetEvolutionLinesUseCase(final EvolutionLineRepository evolutionLineRepository, final ItemDefinitionRepository itemDefinitionRepository) {
        this.evolutionLineRepository = evolutionLineRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
