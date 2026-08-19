package com.dro.modules.event.domain;

import com.dro.modules.player.domain.Player;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventRewardTest {

    @Test
    void pendingRewardIsAvailableBeforeExpiration() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 12, 0);
        EventReward reward = reward(createdAt, createdAt.plusDays(7), EventRewardStatus.PENDING);

        assertTrue(reward.isPendingAt(createdAt.plusDays(1)));
        assertFalse(reward.isPendingAt(createdAt.plusDays(7)));
    }

    @Test
    void claimedRewardIsNeverAvailableAgain() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 12, 0);
        EventReward reward = reward(createdAt, createdAt.plusDays(7), EventRewardStatus.CLAIMED);

        assertFalse(reward.isPendingAt(createdAt.plusDays(1)));
    }

    private EventReward reward(
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            EventRewardStatus status
    ) {
        return EventReward.builder()
                .id(UUID.randomUUID())
                .player(Player.builder().id(UUID.randomUUID()).build())
                .sourceType("EVENT")
                .sourceId("manual-test-001")
                .subject("Premiação")
                .body("Mensagem de teste")
                .bitsAmount(100)
                .itemQuantity(0)
                .status(status)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }
}
