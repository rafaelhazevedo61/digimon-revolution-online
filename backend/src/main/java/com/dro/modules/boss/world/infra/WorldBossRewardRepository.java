package com.dro.modules.boss.world.infra;

import com.dro.modules.boss.world.domain.WorldBossReward;
import com.dro.modules.boss.world.domain.WorldBossRewardType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistência das concessões de Baús do Boss Mundial.
 */
public interface WorldBossRewardRepository extends JpaRepository<WorldBossReward, UUID> {

    Optional<WorldBossReward> findByEventKey(String eventKey);

    @EntityGraph(attributePaths = {"chestDefinition"})
    List<WorldBossReward> findBySourceAttackIdOrderByRewardTypeAsc(UUID sourceAttackId);

    @EntityGraph(attributePaths = {"chestDefinition"})
    List<WorldBossReward> findByWorldBossIdAndRecipientPlayerIdOrderByCreatedAtAsc(
            UUID worldBossId,
            UUID recipientPlayerId
    );

    boolean existsByWorldBossIdAndRewardType(UUID worldBossId, WorldBossRewardType rewardType);
}
