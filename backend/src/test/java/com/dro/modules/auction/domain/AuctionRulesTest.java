package com.dro.modules.auction.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuctionRulesTest {

    @Test
    void calculateSellerFee_usesFivePercentRoundedDown() {
        assertEquals(50, AuctionRules.calculateSellerFee(1000));
        assertEquals(50, AuctionRules.calculateSellerFee(1001));
    }

    @Test
    void calculateSellerFee_usesDurationRate() {
        assertEquals(50, AuctionRules.calculateSellerFee(1000, 500));
        assertEquals(75, AuctionRules.calculateSellerFee(1000, 750));
        assertEquals(100, AuctionRules.calculateSellerFee(1000, 1000));
    }

    @Test
    void sellerFeeRateBpsForDuration_returnsProgressiveRates() {
        assertEquals(500, AuctionRules.sellerFeeRateBpsForDuration(24));
        assertEquals(750, AuctionRules.sellerFeeRateBpsForDuration(48));
        assertEquals(1000, AuctionRules.sellerFeeRateBpsForDuration(72));
    }

    @Test
    void calculateGrossAmount_rejectsOverflow() {
        assertThrows(RuntimeException.class,
                () -> AuctionRules.calculateGrossAmount(Integer.MAX_VALUE, 2));
    }

    @Test
    void activeListingLimit_isTen() {
        assertEquals(10, AuctionRules.MAX_ACTIVE_LISTINGS_PER_PLAYER);
    }

    @Test
    void validateListing_acceptsAllowedDurations() {
        AuctionRules.validateListing(1, 100, 24);
        AuctionRules.validateListing(1, 100, 48);
        AuctionRules.validateListing(1, 100, 72);
    }

    @Test
    void validateListing_rejectsUnsupportedDuration() {
        assertThrows(RuntimeException.class,
                () -> AuctionRules.validateListing(1, 100, 12));
    }

    @Test
    void expirationAt_addsDurationToCreationTime() {
        Instant createdAt = Instant.parse("2026-08-17T00:00:00Z");
        assertEquals(
                Instant.parse("2026-08-19T00:00:00Z"),
                AuctionRules.expirationAt(createdAt, 48)
        );
    }
}
