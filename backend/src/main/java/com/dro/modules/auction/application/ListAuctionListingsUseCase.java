package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.response.AuctionListingPageResponse;
import com.dro.modules.auction.domain.AuctionListingMapper;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.infra.AuctionListingRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListAuctionListingsUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final AuctionListingRepository auctionListingRepository;
    private final PlayerRepository playerRepository;

    public AuctionListingPageResponse execute(
            String token,
            String search,
            String category,
            String rarity,
            int page,
            int size
    ) {
        TokenExtractor.extractPlayerId(token);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE)
        );

        String normalizedSearch = normalize(search);
        String normalizedCategory = normalize(category);
        String normalizedRarity = normalize(rarity);

        Page<com.dro.modules.auction.domain.AuctionListing> listings =
                auctionListingRepository.searchActive(
                        AuctionListingStatus.ACTIVE,
                        Instant.now(),
                        normalizedSearch,
                        normalizedCategory,
                        normalizedRarity,
                        pageable
                );

        var sellerIds = listings.getContent().stream()
                .map(com.dro.modules.auction.domain.AuctionListing::getSellerPlayerId)
                .distinct()
                .toList();

        return AuctionListingMapper.toPageResponse(
                listings,
                sellerIds.isEmpty() ? java.util.List.of() : playerRepository.findAllById(sellerIds)
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
