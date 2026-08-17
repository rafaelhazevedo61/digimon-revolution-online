package com.dro.modules.auction.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionListingTest {

    @Test
    void isActiveAt_returnsTrueOnlyForActiveUnexpiredListingsWithStock() {
        Instant createdAt = Instant.parse("2026-08-17T00:00:00Z");
        Instant expiresAt = Instant.parse("2026-08-19T00:00:00Z");
        AuctionListing listing = AuctionListing.builder()
                .id(UUID.randomUUID())
                .quantity(10)
                .remainingQuantity(10)
                .unitPrice(100)
                .status(AuctionListingStatus.ACTIVE)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .updatedAt(createdAt)
                .build();

        assertTrue(listing.isActiveAt(Instant.parse("2026-08-18T00:00:00Z")));
        assertFalse(listing.isActiveAt(expiresAt));

        listing.setRemainingQuantity(0);
        assertFalse(listing.isActiveAt(Instant.parse("2026-08-18T00:00:00Z")));

        listing.setRemainingQuantity(10);
        listing.setStatus(AuctionListingStatus.SOLD);
        assertFalse(listing.isActiveAt(Instant.parse("2026-08-18T00:00:00Z")));
    }
}
