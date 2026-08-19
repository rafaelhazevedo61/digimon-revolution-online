package com.dro.shared.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditOutboxEventTest {

    @Test
    void pending_startsWithZeroAttemptsAndPendingStatus() {
        AuditOutboxEvent event = AuditOutboxEvent.pending(
                "event-1",
                "MAIL_REWARD_CLAIMED",
                "EventReward",
                "reward-1",
                "request-1",
                "{}"
        );

        assertThat(event.getStatus()).isEqualTo(AuditOutboxStatus.PENDING);
        assertThat(event.getAttempts()).isZero();
        assertThat(event.getEventId()).isEqualTo("event-1");
        assertThat(event.getAvailableAt()).isEqualTo(event.getCreatedAt());
    }

    @Test
    void markFailed_incrementsAttemptsAndSchedulesRetry() {
        AuditOutboxEvent event = AuditOutboxEvent.pending(
                "event-1",
                "SHOP_PURCHASED",
                "ShopProduct",
                "product-1",
                null,
                "{\"summary\":\"purchase\"}"
        );
        Instant retryAt = Instant.parse("2026-08-19T01:00:00Z");

        event.markFailed("Mongo unavailable", retryAt);

        assertThat(event.getStatus()).isEqualTo(AuditOutboxStatus.FAILED);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getLastError()).isEqualTo("Mongo unavailable");
        assertThat(event.getAvailableAt()).isEqualTo(retryAt);
    }

    @Test
    void markPublished_clearsPreviousError() {
        AuditOutboxEvent event = AuditOutboxEvent.pending(
                "event-1",
                "SHOP_PURCHASED",
                "ShopProduct",
                "product-1",
                null,
                Map.of().toString()
        );
        event.markFailed("temporary failure", Instant.now());
        Instant publishedAt = Instant.parse("2026-08-19T01:00:00Z");

        event.markPublished(publishedAt);

        assertThat(event.getStatus()).isEqualTo(AuditOutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(event.getLastError()).isNull();
    }
}
