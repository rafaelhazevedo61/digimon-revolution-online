package com.dro.modules.ranking.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TrainerRankingJob {
    private static final Logger log = LoggerFactory.getLogger(TrainerRankingJob.class);
    private final TrainerRankingRefresher refresher;
    private final boolean enabled;
    private final long delayMillis;
    public TrainerRankingJob(TrainerRankingRefresher refresher,
            @Value("${dro.ranking.trainers.enabled:true}") boolean enabled,
            @Value("${dro.ranking.trainers.delay-ms:300000}") long delayMillis) {
        this.refresher = refresher; this.enabled = enabled; this.delayMillis = Math.max(1000, delayMillis);
    }

    @Scheduled(fixedDelayString = "${dro.ranking.trainers.delay-ms:300000}",
            initialDelayString = "${dro.ranking.trainers.initial-delay-ms:10000}")
    public void run() {
        if (!enabled) return;
        try { refresher.refresh(delayMillis); }
        catch (RuntimeException exception) { log.error("Trainer ranking refresh failed; keeping previous snapshot", exception); }
    }
}
