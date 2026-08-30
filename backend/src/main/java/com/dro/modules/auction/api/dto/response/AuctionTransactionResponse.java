package com.dro.modules.auction.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Casa de Leilões.
 */
public record AuctionTransactionResponse(
        UUID id,
        UUID listingId,
        String direction,
        String itemCode,
        String itemName,
        String buyerUsername,
        String sellerUsername,
        int quantity,
        int unitPrice,
        int grossAmount,
        int fee,
        int sellerNetAmount,
        Instant createdAt
) {
}
