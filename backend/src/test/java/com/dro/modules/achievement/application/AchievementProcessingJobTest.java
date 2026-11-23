package com.dro.modules.achievement.application;

import com.dro.modules.achievement.infra.AchievementQueueRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;

class AchievementProcessingJobTest {
    final AchievementProcessor processor = mock(AchievementProcessor.class);
    final AchievementQueueRepository queue = mock(AchievementQueueRepository.class);

    @Test void disabledJobDoesNothing() {
        new AchievementProcessingJob(processor, queue, false, 100, 60000).run();
        verifyNoInteractions(processor, queue);
    }

    @Test void neverExceedsTheConfiguredBatch() {
        when(processor.processNext()).thenReturn(true);
        new AchievementProcessingJob(processor, queue, true, 100, 60000).run();
        verify(processor, times(100)).processNext();
    }

    @Test void stopsImmediatelyWhenNoPendingAccountRemains() {
        when(processor.processNext()).thenReturn(false);
        new AchievementProcessingJob(processor, queue, true, 100, 60000).run();
        verify(processor).processNext();
    }

    @Test void postponesOneFailedAccountAndContinuesWithOtherAccounts() {
        UUID player = UUID.randomUUID();
        when(processor.processNext()).thenThrow(new AchievementProcessor.ProcessingFailure(player,
                new IllegalStateException("transient"))).thenReturn(true).thenReturn(false);
        new AchievementProcessingJob(processor, queue, true, 100, 60000).run();
        verify(queue).defer(player, 60000);
        verify(processor, times(3)).processNext();
    }
}
