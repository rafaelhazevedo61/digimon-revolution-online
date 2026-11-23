package com.dro.modules.ranking.application;

import com.dro.modules.ranking.infra.TrainerRankingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class TrainerRankingRefresher {
    private final TrainerRankingRepository repository;
    public TrainerRankingRefresher(TrainerRankingRepository repository) { this.repository = repository; }

    @Transactional(timeout = 120, isolation = Isolation.REPEATABLE_READ)
    public boolean refresh(long intervalMillis) {
        if (!repository.lockIfDue(intervalMillis)) return false;
        repository.rebuild();
        repository.captureDailyHistory();
        return true;
    }
}
