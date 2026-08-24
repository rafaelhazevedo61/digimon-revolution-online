package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Componente da camada de serviço de aplicação do módulo de Boss Mundial.
 */
@Service
public class WorldBossService {
    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final WorldBossInstanceFactory worldBossInstanceFactory;

    @Transactional
    public WorldBossInstance getOrCreateToday() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(today).orElseGet(() -> createOrRecover(today));
    }

    private WorldBossInstance createOrRecover(LocalDate today) {
        try {
            return worldBossInstanceFactory.create(today);
        } catch (DataIntegrityViolationException exception) {
            return worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(today).orElseThrow(() -> exception);
        }
    }

    public WorldBossService(final WorldBossInstanceRepository worldBossInstanceRepository, final WorldBossInstanceFactory worldBossInstanceFactory) {
        this.worldBossInstanceRepository = worldBossInstanceRepository;
        this.worldBossInstanceFactory = worldBossInstanceFactory;
    }
}
