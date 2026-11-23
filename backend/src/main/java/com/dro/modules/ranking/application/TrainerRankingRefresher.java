package com.dro.modules.ranking.application;

import com.dro.modules.ranking.infra.TrainerRankingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerRankingRefresher {
    private final TrainerRankingRepository repository;
    public TrainerRankingRefresher(TrainerRankingRepository repository) { this.repository = repository; }

    @Transactional(timeout = 120)
    public boolean refresh(long intervalMillis) {
        if (!repository.lockIfDue(intervalMillis)) return false;
        repository.rebuild();
        return true;
    }
}
