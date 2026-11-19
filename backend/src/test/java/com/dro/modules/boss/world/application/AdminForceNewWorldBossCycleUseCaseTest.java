package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminForceNewWorldBossCycleUseCaseTest {

    @Mock
    private WorldBossInstanceRepository worldBossInstanceRepository;

    @Mock
    private WorldBossInstanceFactory worldBossInstanceFactory;

    @InjectMocks
    private AdminForceNewWorldBossCycleUseCase useCase;

    @Test
    void opensNextCycleAfterCurrentBossIsDefeated() {
        WorldBossInstance defeated = instance(1, WorldBossStatus.DEFEATED);
        WorldBossInstance nextCycle = instance(2, WorldBossStatus.ACTIVE);
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.of(defeated));
        when(worldBossInstanceFactory.create(any(LocalDate.class), eq(2)))
                .thenReturn(nextCycle);

        WorldBossInstance result = useCase.execute();

        assertThat(result).isSameAs(nextCycle);
        assertThat(defeated.getStatus()).isEqualTo(WorldBossStatus.DEFEATED);
        verify(worldBossInstanceFactory).create(any(LocalDate.class), eq(2));
    }

    @Test
    void rejectsOpeningAnotherCycleWhileCurrentBossIsActive() {
        WorldBossInstance active = instance(1, WorldBossStatus.ACTIVE);
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(com.dro.shared.exception.ConflictException.class)
                .hasMessageContaining("só pode ser aberto depois que o Boss Mundial atual for derrotado");

        verifyNoInteractions(worldBossInstanceFactory);
    }

    @Test
    void rejectsOpeningCycleWhenThereIsNoInstanceToday() {
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(com.dro.shared.exception.ConflictException.class)
                .hasMessageContaining("Não existe uma instância do Boss Mundial hoje");

        verifyNoInteractions(worldBossInstanceFactory);
    }

    @Test
    void convertsDuplicateCycleCreationIntoConflict() {
        WorldBossInstance defeated = instance(1, WorldBossStatus.DEFEATED);
        when(worldBossInstanceRepository.findFirstByBossDateOrderByCreatedAtDesc(any(LocalDate.class)))
                .thenReturn(Optional.of(defeated));
        when(worldBossInstanceFactory.create(any(LocalDate.class), eq(2)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate cycle"));

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(com.dro.shared.exception.ConflictException.class)
                .hasMessageContaining("Já existe um novo ciclo do Boss Mundial");
    }

    private WorldBossInstance instance(int cycleNumber, WorldBossStatus status) {
        Instant now = Instant.now();
        return WorldBossInstance.builder()
                .id(java.util.UUID.randomUUID())
                .bossId(1L)
                .bossDate(LocalDate.now())
                .cycleNumber(cycleNumber)
                .maxHp(1_000_000)
                .remainingHp(status == WorldBossStatus.DEFEATED ? 0 : 1_000_000)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .dailyResetAt(now)
                .build();
    }
}
