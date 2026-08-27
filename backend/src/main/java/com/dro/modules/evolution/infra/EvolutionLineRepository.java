package com.dro.modules.evolution.infra;

import com.dro.modules.evolution.domain.EvolutionLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Componente da camada de repositório de persistência do módulo de Evolução.
 */
public interface EvolutionLineRepository extends JpaRepository<EvolutionLine, Long>, JpaSpecificationExecutor<EvolutionLine> {

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
            "content",
            "steps",
            "steps.digimonInfo",
            "steps.materials"
    })
    List<EvolutionLine> findByActiveTrueAndContentActiveTrueAndSteps_DigimonInfo_Id(Long digimonInfoId);

    @EntityGraph(attributePaths = {
            "content",
            "steps",
            "steps.digimonInfo",
            "steps.materials"
    })
    Optional<EvolutionLine> findByIdAndActiveTrueAndContentActiveTrue(Long id);

    @EntityGraph(attributePaths = {
            "steps",
            "steps.digimonInfo",
            "steps.materials"
    })
    Optional<EvolutionLine> findByIdAndActiveTrue(Long id);

    Optional<EvolutionLine> findByCode(String code);

}