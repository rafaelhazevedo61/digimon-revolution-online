package com.dro.modules.auction.domain;

import com.dro.shared.exception.BadRequestException;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@UtilityClass
public class AuctionRules {

    public static final int LISTING_FEE = 100;
    public static final double SELLER_FEE_RATE = 0.05;
    public static final int MAX_ACTIVE_LISTINGS_PER_PLAYER = 20;
    public static final Set<Integer> ALLOWED_DURATIONS_HOURS = Set.of(24, 48, 72);

    public static void validateListing(int quantity, int unitPrice, int durationHours) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (unitPrice <= 0) {
            throw new BadRequestException("Unit price must be greater than zero");
        }
        if (!ALLOWED_DURATIONS_HOURS.contains(durationHours)) {
            throw new BadRequestException("Duration must be 24, 48 or 72 hours");
        }
    }

    public static int calculateGrossAmount(int quantity, int unitPrice) {
        long gross = (long) quantity * unitPrice;
        if (gross > Integer.MAX_VALUE) {
            throw new BadRequestException("Total amount is too high");
        }
        return (int) gross;
    }

    public static int calculateSellerFee(int grossAmount) {
        return Math.max(0, (int) Math.floor(grossAmount * SELLER_FEE_RATE));
    }

    public static Instant expirationAt(Instant createdAt, int durationHours) {
        return createdAt.plus(Duration.ofHours(durationHours));
    }
}
