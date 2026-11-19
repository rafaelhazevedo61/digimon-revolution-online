package com.dro.shared.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineCacheConfigurationTest {

    @Test
    void cacheManager_registersSafeCatalogCaches() throws Exception {
        CacheManager manager = initializedManager();

        assertThat(manager.getCache("shopCatalog")).isNotNull();
        assertThat(manager.getCache("itemDefinitions")).isNotNull();
        assertThat(manager.getCache("equipmentTemplates")).isNotNull();
    }

    @Test
    void catalogCache_canStoreAndEvictValue() throws Exception {
        CacheManager manager = initializedManager();
        Cache cache = manager.getCache("shopCatalog");

        cache.put("active", "catalog-v1");
        assertThat(cache.get("active", String.class)).isEqualTo("catalog-v1");

        cache.evict("active");

        assertThat(cache.get("active")).isNull();
    }

    private static CacheManager initializedManager() throws Exception {
        CaffeineCacheConfiguration configuration = new CaffeineCacheConfiguration();
        org.springframework.cache.support.SimpleCacheManager manager =
                (org.springframework.cache.support.SimpleCacheManager) configuration.cacheManager(10, 60);
        manager.afterPropertiesSet();
        return manager;
    }
}
