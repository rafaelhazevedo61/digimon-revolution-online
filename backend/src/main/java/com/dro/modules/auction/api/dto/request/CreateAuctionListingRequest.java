package com.dro.modules.auction.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Casa de Leilões.
 */
public record CreateAuctionListingRequest(
        @NotNull Long itemDefinitionId,
        @Min(1) int quantity,
        @Min(1) int unitPrice,
        @Min(1) int durationHours
) {
}
