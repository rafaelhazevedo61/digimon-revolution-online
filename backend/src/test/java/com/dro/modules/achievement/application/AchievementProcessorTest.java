package com.dro.modules.achievement.application;

import com.dro.modules.achievement.api.AchievementDtos.Reward;
import com.dro.modules.achievement.infra.AchievementQueueRepository;
import com.dro.modules.achievement.infra.AchievementRepository;
import com.dro.modules.achievement.infra.AchievementRepository.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AchievementProcessorTest {
    final AchievementRepository repository = mock(AchievementRepository.class);
    final AchievementQueueRepository queue = mock(AchievementQueueRepository.class);
    final AchievementProcessor processor = new AchievementProcessor(queue, repository);
    final UUID player = UUID.randomUUID();
    final Definition definition = new Definition("M10", "MISSOES", "Missões", "Resgate 10.", "MISSIONS", 10,
            new Reward(100, "ENERGY_CELL", "Célula", 1));

    void pending(Map<String, Progress> stored) {
        when(queue.lockNext()).thenReturn(Optional.of(player));
        when(repository.definitions()).thenReturn(List.of(definition));
        when(repository.progress(player)).thenReturn(stored);
    }

    @Test void emptyQueueDoesNotReadAnyMetricsOrCatalog() {
        when(queue.lockNext()).thenReturn(Optional.empty());
        assertFalse(processor.processNext());
        verifyNoInteractions(repository);
    }

    @Test void progressChangesWriteOnlyObservedValueAndFinishTheQueueEntry() {
        pending(Map.of("M10", new Progress(2, null, null)));
        when(repository.metrics(player)).thenReturn(Map.of("MISSIONS", 4L));
        assertTrue(processor.processNext());
        verify(repository).observe(player, definition, 4);
        verify(queue).complete(player);
    }

    @Test void unchangedObservationDoesNotWriteProgress() {
        pending(Map.of("M10", new Progress(4, null, null)));
        when(repository.metrics(player)).thenReturn(Map.of("MISSIONS", 4L));
        assertTrue(processor.processNext());
        verify(repository, never()).observe(any(), any(), anyLong());
        verify(queue).complete(player);
    }

    @Test void completedAchievementsAreNotReevaluated() {
        pending(Map.of("M10", new Progress(10, Instant.now(), null)));
        assertTrue(processor.processNext());
        verify(repository, never()).metrics(any());
        verify(repository, never()).observe(any(), any(), anyLong());
        verify(queue).complete(player);
    }

    @Test void observationsAreCappedAndCanCompleteAnAchievement() {
        pending(Map.of("M10", new Progress(9, null, null)));
        when(repository.metrics(player)).thenReturn(Map.of("MISSIONS", 100L));
        assertTrue(processor.processNext());
        verify(repository).observe(player, definition, 10);
    }

    @Test void lowerObservationNeverDecreasesProgress() {
        pending(Map.of("M10", new Progress(9, null, null)));
        when(repository.metrics(player)).thenReturn(Map.of("MISSIONS", 1L));
        assertTrue(processor.processNext());
        verify(repository, never()).observe(any(), any(), anyLong());
    }

    @Test void failureIdentifiesTheAccountAndDoesNotRemoveItsPendingEntry() {
        pending(Map.of());
        when(repository.metrics(player)).thenThrow(new IllegalStateException("transient"));
        var failure = assertThrows(AchievementProcessor.ProcessingFailure.class, processor::processNext);
        assertEquals(player, failure.playerId());
        verify(queue, never()).complete(any());
    }
}
