package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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

    @InjectMocks
    private WorldBossService service;

    @Test
    void getOrCreateToday_returnsExistingInstanceForToday() {
        WorldBossInstance existing = new WorldBossInstance();
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.of(existing));

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(existing, result);
        verifyNoInteractions(worldBossInstanceFactory);
    }

    @Test
    void getOrCreateToday_recoversInstanceWhenAnotherRequestCreatesItFirst() {
        WorldBossInstance existing = new WorldBossInstance();
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(worldBossInstanceFactory.create(any(LocalDate.class)))
                .thenThrow(new DataIntegrityViolationException("daily instance already exists"));

        WorldBossInstance result = service.getOrCreateToday();

        assertSame(existing, result);
        verify(worldBossInstanceFactory).create(any(LocalDate.class));
        verify(worldBossInstanceRepository, times(2))
                .findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class));
    }
}
