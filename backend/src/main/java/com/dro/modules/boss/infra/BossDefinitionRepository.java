package com.dro.modules.boss.infra;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Componente da camada de repositório de persistência do módulo de Boss Mundial.
 */
@Repository
public interface BossDefinitionRepository extends JpaRepository<BossDefinitionEntity, Long> {

    Optional<BossDefinitionEntity> findByCode(String code);

    @EntityGraph(attributePaths = {
            "drops",
            "chestDefinition",
            "worldAttemptChestDefinition",
            "worldTopDamageChestDefinition",
            "worldFinalBlowChestDefinition"
    })
    Optional<BossDefinitionEntity> findWithDropsAndChestById(Long id);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM BossDefinitionEntity b
            LEFT JOIN b.chestDefinition legacyChest
            LEFT JOIN b.worldAttemptChestDefinition attemptChest
            LEFT JOIN b.worldTopDamageChestDefinition topDamageChest
            LEFT JOIN b.worldFinalBlowChestDefinition finalBlowChest
            WHERE legacyChest.id = :chestDefinitionId
               OR attemptChest.id = :chestDefinitionId
               OR topDamageChest.id = :chestDefinitionId
               OR finalBlowChest.id = :chestDefinitionId
            """)
    boolean existsByAnyChestDefinitionId(@Param("chestDefinitionId") Long chestDefinitionId);

    @EntityGraph(attributePaths = {
            "drops",
            "chestDefinition",
            "worldAttemptChestDefinition",
            "worldTopDamageChestDefinition",
            "worldFinalBlowChestDefinition"
    })
    @Query("SELECT b FROM BossDefinitionEntity b WHERE b.active = true ORDER BY b.requiredLevel ASC, b.id ASC")
    List<BossDefinitionEntity> findAllActive();

    @EntityGraph(attributePaths = {
            "drops",
            "chestDefinition",
            "worldAttemptChestDefinition",
            "worldTopDamageChestDefinition",
            "worldFinalBlowChestDefinition"
    })
    List<BossDefinitionEntity> findAllByOrderByIdAsc();
}
