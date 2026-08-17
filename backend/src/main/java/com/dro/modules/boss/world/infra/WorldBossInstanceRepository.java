package com.dro.modules.boss.world.infra;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorldBossInstanceRepository extends JpaRepository<WorldBossInstance, UUID> {

    Optional<WorldBossInstance> findFirstByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Instant since);

    Optional<WorldBossInstance> findFirstByOrderByCreatedAtDesc();
}
