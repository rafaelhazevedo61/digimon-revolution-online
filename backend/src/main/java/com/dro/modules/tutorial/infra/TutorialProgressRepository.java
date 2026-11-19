package com.dro.modules.tutorial.infra;

import com.dro.modules.tutorial.domain.TutorialProgress;
import com.dro.modules.tutorial.domain.TutorialStep;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Tutorial.
 */
public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, UUID> {

    List<TutorialProgress> findByPlayerId(UUID playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select progress from TutorialProgress progress where progress.playerId = :playerId")
    List<TutorialProgress> findByPlayerIdForUpdate(@Param("playerId") UUID playerId);

    boolean existsByPlayerIdAndStep(UUID playerId, TutorialStep step);
}
