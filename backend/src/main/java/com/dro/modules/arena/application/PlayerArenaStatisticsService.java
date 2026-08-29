package com.dro.modules.arena.application;

import com.dro.modules.arena.domain.PlayerArenaStatistics;
import com.dro.modules.arena.infra.PlayerArenaStatisticsRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PlayerArenaStatisticsService {
    private static final String CACHE_NAME = "playerArenaStatistics";
    private final PlayerArenaStatisticsRepository repository;

    public PlayerArenaStatisticsService(PlayerArenaStatisticsRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#playerId")
    @Transactional(readOnly = true)
    public PlayerArenaStatistics get(UUID playerId) {
        return repository.findById(playerId).orElseGet(() -> new PlayerArenaStatistics(playerId));
    }

    @CachePut(cacheNames = CACHE_NAME, key = "#playerId")
    @Transactional
    public PlayerArenaStatistics recordResult(UUID playerId, int ratingChange, boolean victory) {
        PlayerArenaStatistics statistics = repository.findByPlayerId(playerId)
                .orElseGet(() -> new PlayerArenaStatistics(playerId));
        if (victory) {
            statistics.recordWin(ratingChange);
        } else {
            statistics.recordLoss(Math.abs((long) ratingChange));
        }
        return repository.save(statistics);
    }
}
