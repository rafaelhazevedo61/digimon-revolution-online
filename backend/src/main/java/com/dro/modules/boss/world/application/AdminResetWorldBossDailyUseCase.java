package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Boss Mundial.
 */
@Service
public class AdminResetWorldBossDailyUseCase {
    private final WorldBossInstanceRepository worldBossInstanceRepository;

    @Transactional
    public int execute() {
        LocalDate todayDate = LocalDate.now(ZoneId.systemDefault());
        Instant now = Instant.now();
        Optional<WorldBossInstance> today = worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(todayDate);
        if (today.isEmpty()) {
            return 0;
        }
        WorldBossInstance instance = today.get();
        instance.setDailyResetAt(now);
        instance.setUpdatedAt(now);
        worldBossInstanceRepository.save(instance);
        return 1;
    }

    public AdminResetWorldBossDailyUseCase(final WorldBossInstanceRepository worldBossInstanceRepository) {
        this.worldBossInstanceRepository = worldBossInstanceRepository;
    }
}
