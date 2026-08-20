package com.dro.modules.loot.infra;

import com.dro.modules.loot.domain.LootTableEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso ao catálogo persistido de loot tables.
 */
public interface LootTableRepository extends JpaRepository<LootTableEntity, Long> {

    Optional<LootTableEntity> findByCode(String code);

    Optional<LootTableEntity> findByCodeAndActiveTrue(String code);

    @EntityGraph(attributePaths = {"rarityWeights", "entries"})
    Optional<LootTableEntity> findWithWeightsAndEntriesByCode(String code);

    List<LootTableEntity> findAllByOrderByNameAsc();

    List<LootTableEntity> findByActiveTrueOrderByNameAsc();
}
