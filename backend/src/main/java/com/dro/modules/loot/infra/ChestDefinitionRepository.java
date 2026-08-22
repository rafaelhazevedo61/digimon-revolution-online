package com.dro.modules.loot.infra;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso ao catálogo persistido de baús temáticos.
 */
public interface ChestDefinitionRepository extends JpaRepository<ChestDefinitionEntity, Long> {

    Optional<ChestDefinitionEntity> findByCode(String code);

    Optional<ChestDefinitionEntity> findByCodeAndActiveTrue(String code);

    boolean existsByLootTable_Id(Long lootTableId);

    @EntityGraph(attributePaths = {"lootTable", "itemDefinition"})
    Optional<ChestDefinitionEntity> findWithCatalogByCode(String code);

    @EntityGraph(attributePaths = {"lootTable", "itemDefinition"})
    List<ChestDefinitionEntity> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"lootTable", "itemDefinition"})
    List<ChestDefinitionEntity> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"lootTable", "itemDefinition"})
    List<ChestDefinitionEntity> findByActiveTrueAndCodeStartingWithOrderByNameAsc(String codePrefix);
}
