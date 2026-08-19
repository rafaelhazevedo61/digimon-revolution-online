package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.response.AuctionListingPageResponse;
import com.dro.modules.auction.domain.AuctionListingMapper;
import com.dro.modules.auction.infra.AuctionListingRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Casa de Leilões.
 */
@Service
@RequiredArgsConstructor
public class ListMyAuctionListingsUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final AuctionListingRepository auctionListingRepository;
    private final PlayerRepository playerRepository;

    public AuctionListingPageResponse execute(String token, int page, int size) {
        var playerId = TokenExtractor.extractPlayerId(token);
        var pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE)
        );
        Page<com.dro.modules.auction.domain.AuctionListing> listings =
                auctionListingRepository.findSellerListings(playerId, pageable);

        var player = playerRepository.findById(playerId).orElse(null);
        return AuctionListingMapper.toPageResponse(
                listings,
                player == null ? java.util.List.of() : java.util.List.of(player)
        );
    }
}
