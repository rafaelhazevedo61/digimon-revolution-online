package com.dro.shared.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configuração dos caches locais para catálogos de leitura segura.
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfiguration {

    @Bean
    public CacheManager cacheManager(
            @Value("${dro.cache.catalogs.maximum-size:500}") long maximumSize,
            @Value("${dro.cache.catalogs.ttl-seconds:300}") long ttlSeconds
    ) {
        Duration ttl = Duration.ofSeconds(Math.max(1, ttlSeconds));
        List<CaffeineCache> caches = List.of(
                createCache("shopCatalog", maximumSize, ttl),
                createCache("itemDefinitions", maximumSize, ttl),
                createCache("equipmentTemplates", maximumSize, ttl),
                createCache("playerPaginationPreferences", maximumSize, ttl),
                createCache("playerShortcutPreferences", maximumSize, ttl),
                createCache("playerArenaStatistics", maximumSize, ttl)
        );
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    private CaffeineCache createCache(String name, long maximumSize, Duration ttl) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .maximumSize(Math.max(1, maximumSize))
                        .expireAfterWrite(ttl)
                        .recordStats()
                        .build()
        );
    }
}
