package com.dro.modules.evolution.infra;

import com.dro.modules.evolution.domain.EvolutionLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvolutionLineRepository extends JpaRepository<EvolutionLine, Long> {

    @EntityGraph(attributePaths = {
            "content",
            "steps",
            "steps.digimonInfo"
    })
    List<EvolutionLine> findByActiveTrueAndContentActiveTrue();
}