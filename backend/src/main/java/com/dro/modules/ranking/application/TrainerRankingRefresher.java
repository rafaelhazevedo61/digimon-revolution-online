package com.dro.modules.ranking.application;

import com.dro.modules.ranking.infra.TrainerRankingRepository;
import com.dro.modules.ranking.infra.TrainerRankingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class TrainerRankingRefresher {
    private final TrainerRankingRepository repository;
    private final TrainerRankingProgressRepository progress;
    public TrainerRankingRefresher(TrainerRankingRepository repository, TrainerRankingProgressRepository progress) {
        this.repository = repository; this.progress = progress;
    }

    @Transactional(timeout = 120, isolation = Isolation.REPEATABLE_READ)
    public boolean refresh(long intervalMillis) {
        if (!repository.lockIfDue(intervalMillis)) return false;
        repository.rebuild();
        repository.captureDailyHistory();
        progress.publish();
        return true;
    }
}
