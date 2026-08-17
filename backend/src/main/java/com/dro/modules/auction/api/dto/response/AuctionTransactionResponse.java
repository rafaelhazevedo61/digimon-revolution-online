package com.dro.modules.auction.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuctionTransactionResponse(
        UUID id,
        UUID listingId,
        String direction,
        String itemCode,
        String itemName,
        int quantity,
        int unitPrice,
        int grossAmount,
        int fee,
        int sellerNetAmount,
        Instant createdAt
) {
}
