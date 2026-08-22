package com.dro.modules.tutorial.infra;

import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Tutorial.
 */
public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, UUID> {

    List<TutorialProgress> findByPlayerId(UUID playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TutorialProgress> findByPlayerIdForUpdate(UUID playerId);

    boolean existsByPlayerIdAndStep(UUID playerId, TutorialStep step);
}
