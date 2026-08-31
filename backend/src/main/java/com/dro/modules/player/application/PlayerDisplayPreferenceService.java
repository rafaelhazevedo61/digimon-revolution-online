package com.dro.modules.player.application;

import com.dro.modules.player.domain.PlayerDisplayPreference;
import com.dro.modules.player.infra.PlayerDisplayPreferenceRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PlayerDisplayPreferenceService {
    private static final boolean DEFAULT_PAGINATION_ENABLED = true;
    private final PlayerDisplayPreferenceRepository repository;

    @Cacheable(cacheNames = "playerPaginationPreferences", key = "#playerId")
    @Transactional(readOnly = true)
    public PlayerDisplayPreference get(UUID playerId) {
        return repository.findById(playerId)
                .orElseGet(() -> new PlayerDisplayPreference(playerId, DEFAULT_PAGINATION_ENABLED, Instant.now()));
    }

    @CachePut(cacheNames = "playerPaginationPreferences", key = "#playerId")
    @Transactional
    public PlayerDisplayPreference set(UUID playerId, boolean paginationEnabled) {
        PlayerDisplayPreference preference = repository.findById(playerId)
                .orElseGet(() -> new PlayerDisplayPreference(playerId, paginationEnabled, Instant.now()));
        preference.setPaginationEnabled(paginationEnabled);
        preference.setUpdatedAt(Instant.now());
        return repository.save(preference);
    }

    public PlayerDisplayPreferenceService(PlayerDisplayPreferenceRepository repository) {
        this.repository = repository;
    }
}
