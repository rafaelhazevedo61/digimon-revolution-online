package com.dro.modules.boss.infra;

import com.dro.modules.boss.domain.BossAttemptEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BossAttemptRepository extends JpaRepository<BossAttemptEntity, UUID> {

    @Query("SELECT a FROM BossAttemptEntity a WHERE a.playerId = :playerId AND a.bossId = :bossId ORDER BY a.createdAt DESC")
    List<BossAttemptEntity> findByPlayerIdAndBossIdOrderByCreatedAtDesc(UUID playerId, Long bossId, Pageable pageable);

    @Query("SELECT a FROM BossAttemptEntity a WHERE a.playerId = :playerId ORDER BY a.createdAt DESC")
    List<BossAttemptEntity> findByPlayerIdOrderByCreatedAtDesc(UUID playerId, Pageable pageable);

    @Query("SELECT a FROM BossAttemptEntity a WHERE a.playerId = :playerId AND a.bossId = :bossId AND a.createdAt > :since ORDER BY a.createdAt DESC")
    List<BossAttemptEntity> findRecentAttempts(UUID playerId, Long bossId, Instant since);

    Optional<BossAttemptEntity> findFirstByPlayerIdAndBossIdOrderByCreatedAtDesc(UUID playerId, Long bossId);
}
