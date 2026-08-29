package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorldBossServiceTest {

    @Mock
    private WorldBossInstanceRepository worldBossInstanceRepository;

    @Mock
    private WorldBossInstanceFactory worldBossInstanceFactory;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private WorldBossService service;

    @Test
    void getOrCreateToday_returnsExistingInstanceForToday() {
        WorldBossInstance existing = new WorldBossInstance();
        when(worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(existing));

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(existing, result);
        verifyNoInteractions(worldBossInstanceFactory);
    }

    @Test
    void getOrCreateToday_keepsDefeatedInstanceWhenAutomaticRespawnIsDisabled() {
        WorldBossInstance defeated = new WorldBossInstance();
        defeated.setStatus(WorldBossStatus.DEFEATED);
        when(worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(defeated));
        when(gameplayConfig.isAutoBossRespawnAfterDefeatEnabled()).thenReturn(false);

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(defeated, result);
        verifyNoInteractions(worldBossInstanceFactory);
    }

    @Test
    void getOrCreateToday_createsNextCycleWhenAutomaticRespawnIsEnabled() {
        WorldBossInstance defeated = new WorldBossInstance();
        defeated.setStatus(WorldBossStatus.DEFEATED);
        defeated.setDefeatedAt(Instant.now().minusSeconds(3601));
        defeated.setCycleNumber(2);
        WorldBossInstance nextCycle = new WorldBossInstance();
        when(worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(defeated));
        when(gameplayConfig.isAutoBossRespawnAfterDefeatEnabled()).thenReturn(true);
        when(worldBossInstanceFactory.create(any(LocalDate.class), eq(3))).thenReturn(nextCycle);

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(nextCycle, result);
        verify(worldBossInstanceFactory).create(any(LocalDate.class), eq(3));
    }

    @Test
    void getOrCreateToday_recoversInstanceWhenAnotherRequestCreatesItFirst() {
        WorldBossInstance existing = new WorldBossInstance();
        when(worldBossInstanceRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(worldBossInstanceFactory.create(any(LocalDate.class)))
                .thenThrow(new DataIntegrityViolationException("daily instance already exists"));

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(existing, result);
        verify(worldBossInstanceFactory).create(any(LocalDate.class));
        verify(worldBossInstanceRepository, times(2)).findFirstByOrderByCreatedAtDesc();
    }
}
