package com.dro.modules.achievement.application;

import com.dro.modules.achievement.infra.AchievementQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AchievementProcessingJob {
    private static final Logger log = LoggerFactory.getLogger(AchievementProcessingJob.class);
    private final AchievementProcessor processor;
    private final AchievementQueueRepository queue;
    private final boolean enabled;
    private final int batchSize;
    private final long retryDelayMillis;

    public AchievementProcessingJob(AchievementProcessor processor, AchievementQueueRepository queue,
                                    @Value("${dro.achievements.scheduler.enabled:true}") boolean enabled,
                                    @Value("${dro.achievements.scheduler.batch-size:100}") int batchSize,
                                    @Value("${dro.achievements.scheduler.retry-delay-ms:60000}") long retryDelayMillis) {
        this.processor = processor; this.queue = queue; this.enabled = enabled;
        this.batchSize = Math.max(1, Math.min(1000, batchSize));
        this.retryDelayMillis = Math.max(1000, retryDelayMillis);
    }

    @Scheduled(fixedDelayString = "${dro.achievements.scheduler.delay-ms:30000}",
            initialDelayString = "${dro.achievements.scheduler.initial-delay-ms:30000}")
    public void run() {
        if (!enabled) return;
        for (int index = 0; index < batchSize; index++) {
            try {
                if (!processor.processNext()) return;
            } catch (AchievementProcessor.ProcessingFailure failure) {
                log.warn("Achievement processing failed; retry scheduled. playerId={}", failure.playerId(), failure.getCause());
                try {
                    queue.defer(failure.playerId(), retryDelayMillis);
                } catch (RuntimeException exception) {
                    log.error("Could not postpone achievement processing; stopping batch", exception);
                    return;
                }
            } catch (RuntimeException exception) {
                log.error("Could not read achievement processing queue; stopping batch", exception);
                return;
            }
        }
    }
}
