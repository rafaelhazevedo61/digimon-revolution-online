package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorldBossInstanceFactory {

    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final BossDefinitionRepository bossDefinitionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WorldBossInstance create(LocalDate bossDate) {
        BossDefinitionEntity boss = bossDefinitionRepository.findAllActive().stream()
                .filter(candidate -> candidate.getBossType() == BossType.WORLD)
                .min((a, b) -> {
                    int stageCompare = Integer.compare(
                            a.getRequiredStage().ordinal(),
                            b.getRequiredStage().ordinal()
                    );
                    if (stageCompare != 0) {
                        return stageCompare;
                    }
                    return Integer.compare(a.getRequiredLevel(), b.getRequiredLevel());
                })
                .orElseThrow(() -> new NotFoundException("No world boss is available"));

        Instant now = Instant.now();
        WorldBossInstance instance = WorldBossInstance.builder()
                .id(UUID.randomUUID())
                .bossId(boss.getId())
                .bossDate(bossDate)
                .maxHp(boss.getHp())
                .remainingHp(boss.getHp())
                .status(WorldBossStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .dailyResetAt(now)
                .build();

        return worldBossInstanceRepository.saveAndFlush(instance);
    }
}
