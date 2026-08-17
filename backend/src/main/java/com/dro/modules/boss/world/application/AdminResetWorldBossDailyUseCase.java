package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminResetWorldBossDailyUseCase {

    private final WorldBossInstanceRepository worldBossInstanceRepository;

    @Transactional
    public int execute() {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant now = Instant.now();

        Optional<WorldBossInstance> today = worldBossInstanceRepository
                .findFirstByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(startOfDay);

        if (today.isEmpty()) {
            return 0;
        }

        WorldBossInstance instance = today.get();
        instance.setDailyResetAt(now);
        instance.setUpdatedAt(now);
        worldBossInstanceRepository.save(instance);

        return 1;
    }
}
