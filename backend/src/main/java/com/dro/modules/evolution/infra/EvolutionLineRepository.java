package com.dro.modules.evolution.infra;

import com.dro.modules.evolution.domain.EvolutionLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvolutionLineRepository extends JpaRepository<EvolutionLine, Long> {

    @EntityGraph(attributePaths = {
            "content",
            "steps",
            "steps.digimonInfo"
    })
    List<EvolutionLine> findByActiveTrueAndContentActiveTrue();

    @EntityGraph(attributePaths = {
            "steps",
            "steps.digimonInfo",
            "steps.materials"
    })
    List<EvolutionLine> findByActiveTrueAndSteps_DigimonInfo_Id(Long digimonInfoId);

    @EntityGraph(attributePaths = {
            "steps",
            "steps.digimonInfo",
            "steps.materials"
    })
    Optional<EvolutionLine> findByIdAndActiveTrue(Long id);
}