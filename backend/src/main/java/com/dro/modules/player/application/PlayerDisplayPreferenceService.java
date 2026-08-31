package com.dro.modules.player.application;

import com.dro.modules.player.domain.PlayerDisplayPreference;
import com.dro.modules.player.infra.PlayerDisplayPreferenceRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dro.shared.exception.BadRequestException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PlayerDisplayPreferenceService {
    private static final boolean DEFAULT_PAGINATION_ENABLED = true;
    private static final int MAX_SHORTCUTS = 8;
    private static final List<String> DEFAULT_SHORTCUTS = List.of("dashboard", "missions", "inventory", "shop");
    private static final Set<String> ALLOWED_SHORTCUTS = Set.of(
            "dashboard", "activity-calendar", "missions", "shop", "forge", "auction-house", "mail",
            "inventory", "evolution", "rebirth", "incubation", "pokedex", "bosses", "arena", "ranking",
            "storage", "collection", "clans", "world-boss"
    );
    private final PlayerDisplayPreferenceRepository repository;

    @Cacheable(cacheNames = "playerPaginationPreferences", key = "#playerId")
    @Transactional(readOnly = true)
    public PlayerDisplayPreference get(UUID playerId) {
        return repository.findById(playerId)
                .orElseGet(() -> new PlayerDisplayPreference(playerId, DEFAULT_PAGINATION_ENABLED, String.join(",", DEFAULT_SHORTCUTS), Instant.now()));
    }

    @CachePut(cacheNames = "playerPaginationPreferences", key = "#playerId")
    @Transactional
    public PlayerDisplayPreference set(UUID playerId, boolean paginationEnabled) {
        PlayerDisplayPreference preference = repository.findById(playerId)
                .orElseGet(() -> new PlayerDisplayPreference(playerId, paginationEnabled, "", Instant.now()));
        preference.setPaginationEnabled(paginationEnabled);
        preference.setUpdatedAt(Instant.now());
        return repository.save(preference);
    }

    @Cacheable(cacheNames = "playerShortcutPreferences", key = "#playerId")
    @Transactional(readOnly = true)
    public List<String> getShortcutRoutes(UUID playerId) {
        PlayerDisplayPreference preference = get(playerId);
        return parseShortcutRoutes(preference.getShortcutRoutes());
    }

    @CachePut(cacheNames = "playerShortcutPreferences", key = "#playerId")
    @Transactional
    public List<String> setShortcutRoutes(UUID playerId, List<String> routes) {
        if (routes == null || routes.size() > MAX_SHORTCUTS) {
            throw new BadRequestException("At most 8 shortcuts can be selected");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String route : routes) {
            String value = route == null ? "" : route.trim().toLowerCase();
            if (!ALLOWED_SHORTCUTS.contains(value)) {
                throw new BadRequestException("Invalid shortcut route: " + route);
            }
            normalized.add(value);
        }
        PlayerDisplayPreference preference = repository.findById(playerId)
                .orElseGet(() -> new PlayerDisplayPreference(playerId, DEFAULT_PAGINATION_ENABLED, "", Instant.now()));
        preference.setShortcutRoutes(String.join(",", normalized));
        preference.setUpdatedAt(Instant.now());
        repository.save(preference);
        return new ArrayList<>(normalized);
    }

    private List<String> parseShortcutRoutes(String value) {
        if (value == null || value.isBlank()) return List.of();
        return new ArrayList<>(Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(ALLOWED_SHORTCUTS::contains)
                .distinct()
                .limit(MAX_SHORTCUTS)
                .toList());
    }

    public PlayerDisplayPreferenceService(PlayerDisplayPreferenceRepository repository) {
        this.repository = repository;
    }
}
