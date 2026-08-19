package com.dro.modules.inventory.infra;

import com.dro.modules.inventory.domain.ItemDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Componente da camada de repositório de persistência do módulo de Inventário.
 */
public interface ItemDefinitionRepository extends JpaRepository<ItemDefinition, Long> {

    Optional<ItemDefinition> findByCode(String code);

    @Query("""
            SELECT item
            FROM ItemDefinition item
            WHERE (:category IS NULL OR UPPER(item.category) = :category)
              AND (:rarity IS NULL OR UPPER(item.rarity) = :rarity)
              AND (:usable IS NULL OR item.usable = :usable)
              AND (:sellable IS NULL OR item.sellable = :sellable)
              AND (:tradable IS NULL OR item.tradable = :tradable)
            """)
    Page<ItemDefinition> findCatalog(
            @Param("category") String category,
            @Param("rarity") String rarity,
            @Param("usable") Boolean usable,
            @Param("sellable") Boolean sellable,
            @Param("tradable") Boolean tradable,
            Pageable pageable
    );
}