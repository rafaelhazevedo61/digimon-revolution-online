package com.dro.modules.auction.api.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Contrato de dados do módulo de Casa de Leilões.
 */
public record BuyAuctionListingRequest(
        @Min(1) int quantity
) {
}
