package com.dro.modules.auction.domain;

import com.dro.modules.auction.api.dto.response.AuctionListingPageResponse;
import com.dro.modules.auction.api.dto.response.AuctionListingResponse;
import com.dro.modules.player.domain.Player;
import org.springframework.data.domain.Page;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Componente da camada de conversor entre domínio e contratos da API do módulo de Casa de Leilões.
 */
public final class AuctionListingMapper {

    private AuctionListingMapper() {
    }

    public static AuctionListingPageResponse toPageResponse(Page<AuctionListing> page, Iterable<Player> players) {
        Map<UUID, String> usernames = players == null
                ? Collections.emptyMap()
                : toUsernames(players);

        return new AuctionListingPageResponse(
                page.getContent().stream()
                        .map(listing -> toResponse(listing, usernames.getOrDefault(listing.getSellerPlayerId(), "Unknown")))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static AuctionListingResponse toResponse(AuctionListing listing, String sellerUsername) {
        var item = listing.getItemDefinition();
        int totalRemainingPrice = (int) Math.min(
                Integer.MAX_VALUE,
                (long) listing.getRemainingQuantity() * listing.getUnitPrice()
        );

        return new AuctionListingResponse(
                listing.getId(),
                listing.getSellerPlayerId(),
                sellerUsername,
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getCategory(),
                item.getRarity(),
                item.getIcon(),
                listing.getQuantity(),
                listing.getRemainingQuantity(),
                listing.getUnitPrice(),
                totalRemainingPrice,
                listing.getListingFee(),
                (int) Duration.between(listing.getCreatedAt(), listing.getExpiresAt()).toHours(),
                listing.getSellerFeeRateBps(),
                listing.getStatus(),
                listing.getCreatedAt(),
                listing.getExpiresAt()
        );
    }

    private static Map<UUID, String> toUsernames(Iterable<Player> players) {
        return java.util.stream.StreamSupport.stream(players.spliterator(), false)
                .collect(Collectors.toMap(Player::getId, Player::getUsername));
    }
}
