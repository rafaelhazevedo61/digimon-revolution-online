package com.dro.modules.event.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_rewards", indexes = {
        @Index(name = "idx_event_reward_player_status", columnList = "player_id, status, expires_at"),
        @Index(name = "idx_event_reward_source", columnList = "source_type, source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventReward {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 128)
    private String sourceId;

    @Column(nullable = false, length = 80)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "bits_amount", nullable = false)
    @Builder.Default
    private int bitsAmount = 0;

    @Column(name = "item_type", length = 50)
    private String itemType;

    @Column(name = "item_quantity", nullable = false)
    @Builder.Default
    private int itemQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventRewardStatus status = EventRewardStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    public boolean isPendingAt(LocalDateTime now) {
        return status == EventRewardStatus.PENDING && expiresAt.isAfter(now);
    }
}
