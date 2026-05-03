package com.dro.modules.evolution.infra;

import com.dro.modules.evolution.domain.EvolutionLineStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvolutionLineStepRepository extends JpaRepository<EvolutionLineStep, Long> {

    Optional<EvolutionLineStep> findByEvolutionLineIdAndStepOrder(Long evolutionLineId, int stepOrder);
}
