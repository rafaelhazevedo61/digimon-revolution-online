package com.dro.modules.auction.api.dto.response;

import com.dro.modules.auction.domain.AuctionListingStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Casa de Leilões.
 */
public record AuctionListingResponse(
        UUID id,
        UUID sellerPlayerId,
        String sellerUsername,
        Long itemDefinitionId,
        String itemCode,
        String itemName,
        String category,
        String rarity,
        String icon,
        int quantity,
        int remainingQuantity,
        int unitPrice,
        int totalRemainingPrice,
        int listingFee,
        int durationHours,
        int sellerFeeRateBps,
        AuctionListingStatus status,
        Instant createdAt,
        Instant expiresAt
) {
}
