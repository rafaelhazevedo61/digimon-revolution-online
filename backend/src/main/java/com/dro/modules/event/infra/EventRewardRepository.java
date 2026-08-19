package com.dro.modules.event.infra;

import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EventRewardRepository extends JpaRepository<EventReward, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EventReward r WHERE r.id = :id AND r.player.id = :playerId")
    Optional<EventReward> findByIdAndPlayerIdForUpdate(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );

    Optional<EventReward> findBySourceTypeAndSourceIdAndPlayerId(
            String sourceType,
            String sourceId,
            UUID playerId
    );

    @Modifying
    @Query(value = """
            INSERT INTO event_rewards (
                id, player_id, source_type, source_id, subject, body,
                bits_amount, item_type, item_quantity, status,
                created_at, expires_at
            ) VALUES (
                :id, :playerId, :sourceType, :sourceId, :subject, :body,
                :bitsAmount, :itemType, :itemQuantity, :status,
                :createdAt, :expiresAt
            ) ON CONFLICT (source_type, source_id, player_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId,
            @Param("sourceType") String sourceType,
            @Param("sourceId") String sourceId,
            @Param("subject") String subject,
            @Param("body") String body,
            @Param("bitsAmount") int bitsAmount,
            @Param("itemType") String itemType,
            @Param("itemQuantity") int itemQuantity,
            @Param("status") EventRewardStatus status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("expiresAt") LocalDateTime expiresAt
    );
}
