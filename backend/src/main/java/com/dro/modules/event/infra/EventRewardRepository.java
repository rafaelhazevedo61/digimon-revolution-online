package com.dro.modules.event.infra;

import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventRewardRepository extends JpaRepository<EventReward, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EventReward r WHERE r.id = :id AND r.player.id = :playerId")
    Optional<EventReward> findByIdAndPlayerIdForUpdate(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );

    boolean existsBySourceTypeAndSourceIdAndPlayerId(
            String sourceType,
            String sourceId,
            UUID playerId
    );
}
