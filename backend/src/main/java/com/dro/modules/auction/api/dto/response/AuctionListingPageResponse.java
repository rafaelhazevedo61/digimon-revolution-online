package com.dro.modules.auction.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Casa de Leilões.
 */
public record AuctionListingPageResponse(
        List<AuctionListingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
