package com.dro.modules.auction.api.dto.response;

import com.dro.modules.auction.domain.AuctionListingStatus;

import java.util.UUID;

public record AuctionPurchaseResponse(
        UUID listingId,
        String itemCode,
        String itemName,
        int quantity,
        int grossAmount,
        int fee,
        int totalPaid,
        int sellerNetAmount,
        int remainingQuantity,
        AuctionListingStatus listingStatus,
        int buyerBitsRemaining,
        String message
) {
}
