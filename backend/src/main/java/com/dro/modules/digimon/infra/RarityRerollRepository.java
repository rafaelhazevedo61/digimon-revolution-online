package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.RarityReroll;
import com.dro.modules.digimon.domain.RerollStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RarityRerollRepository extends JpaRepository<RarityReroll, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RarityReroll r WHERE r.id = :id AND r.playerId = :playerId AND r.status = :status")
    Optional<RarityReroll> findPendingForUpdate(@Param("id") UUID id, @Param("playerId") UUID playerId, @Param("status") RerollStatus status);
}
