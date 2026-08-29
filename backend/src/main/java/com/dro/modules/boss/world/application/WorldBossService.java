package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.shared.config.GameplayConfig;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.time.Instant;

/**
 * Componente da camada de serviço de aplicação do módulo de Boss Mundial.
 */
@Service
public class WorldBossService {
    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final WorldBossInstanceFactory worldBossInstanceFactory;
    private final GameplayConfig gameplayConfig;

    private static final Duration RESPAWN_DELAY = Duration.ofHours(1);

    @Transactional
    public WorldBossInstance getOrCreateToday() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        WorldBossInstance current = worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
        if (current == null) {
            return createOrRecover(today);
        }
        if (gameplayConfig.isAutoBossRespawnAfterDefeatEnabled()
                && current.getStatus() == WorldBossStatus.DEFEATED
                && current.getDefeatedAt() != null
                && !Instant.now().isBefore(current.getDefeatedAt().plus(RESPAWN_DELAY))) {
            return createOrRecover(today, current.getCycleNumber() + 1);
        }
        return current;
    }

    private WorldBossInstance createOrRecover(LocalDate today) {
        try {
            return worldBossInstanceFactory.create(today);
        } catch (DataIntegrityViolationException exception) {
            return worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc().orElseThrow(() -> exception);
        }
    }

    private WorldBossInstance createOrRecover(LocalDate today, int cycleNumber) {
        try {
            return worldBossInstanceFactory.create(today, cycleNumber);
        } catch (DataIntegrityViolationException exception) {
            return worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc().orElseThrow(() -> exception);
        }
    }

    public WorldBossService(final WorldBossInstanceRepository worldBossInstanceRepository, final WorldBossInstanceFactory worldBossInstanceFactory, final GameplayConfig gameplayConfig) {
        this.worldBossInstanceRepository = worldBossInstanceRepository;
        this.worldBossInstanceFactory = worldBossInstanceFactory;
        this.gameplayConfig = gameplayConfig;
    }
}
