package com.dro.modules.evolution.application;

import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.evolution.api.dto.response.AvailableEvolutionLineResponse;
import com.dro.modules.evolution.api.dto.response.AvailableEvolutionLineStepResponse;
import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Evolução.
 */
@Service
@RequiredArgsConstructor
public class GetAvailableEvolutionLinesUseCase {

    private final EvolutionLineRepository evolutionLineRepository;

    public List<AvailableEvolutionLineResponse> execute() {
        return evolutionLineRepository.findByActiveTrueAndContentActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AvailableEvolutionLineResponse toResponse(EvolutionLine line) {
        List<AvailableEvolutionLineStepResponse> steps = line.getSteps()
                .stream()
                .sorted(Comparator.comparingInt(EvolutionLineStep::getStepOrder))
                .map(this::toStepResponse)
                .toList();

        return new AvailableEvolutionLineResponse(
                line.getCode(),
                line.getName(),
                line.getDescription(),
                steps
        );
    }

    private AvailableEvolutionLineStepResponse toStepResponse(EvolutionLineStep step) {
        DigimonInfos digimonInfo = step.getDigimonInfo();

        return new AvailableEvolutionLineStepResponse(
                step.getStepOrder(),
                digimonInfo.getId(),
                digimonInfo.getName(),
                step.getStage().name(),
                digimonInfo.getAttribute().name(),
                digimonInfo.getElement().name(),
                digimonInfo.getSpecie().name()
        );
    }
}