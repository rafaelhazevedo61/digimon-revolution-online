package com.dro.modules.arena.infra;

import com.dro.modules.arena.domain.PlayerArenaStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.UUID;

public interface PlayerArenaStatisticsRepository extends JpaRepository<PlayerArenaStatistics, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PlayerArenaStatistics> findByPlayerId(UUID playerId);
}
