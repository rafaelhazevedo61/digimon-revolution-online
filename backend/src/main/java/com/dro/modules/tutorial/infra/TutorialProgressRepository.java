package com.dro.modules.tutorial.infra;

import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, UUID> {

    List<TutorialProgress> findByPlayerId(UUID playerId);

    boolean existsByPlayerIdAndStep(UUID playerId, TutorialStep step);
}
