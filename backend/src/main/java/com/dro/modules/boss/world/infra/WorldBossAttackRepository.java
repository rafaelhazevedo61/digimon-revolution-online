package com.dro.modules.boss.world.infra;

import com.dro.modules.boss.world.domain.WorldBossAttack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Boss Mundial.
 */
@Repository
public interface WorldBossAttackRepository extends JpaRepository<WorldBossAttack, UUID> {

    long countByWorldBossIdAndPlayerIdAndCreatedAtGreaterThanEqual(UUID worldBossId, UUID playerId, Instant since);

    List<WorldBossAttack> findByWorldBossIdOrderByCreatedAtDesc(UUID worldBossId);

    List<WorldBossAttack> findByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(UUID worldBossId, UUID playerId);
}
