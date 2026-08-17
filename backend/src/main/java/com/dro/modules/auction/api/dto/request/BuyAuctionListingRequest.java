package com.dro.modules.auction.api.dto.request;

import jakarta.validation.constraints.Min;

public record BuyAuctionListingRequest(
        @Min(1) int quantity
) {
}
