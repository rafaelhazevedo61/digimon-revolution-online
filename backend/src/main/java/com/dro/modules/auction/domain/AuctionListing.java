package com.dro.modules.auction.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auction_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionListing {

    @Id
    private UUID id;

    @Column(name = "seller_player_id", nullable = false)
    private UUID sellerPlayerId;

    @Column(name = "seller_digimon_id")
    private UUID sellerDigimonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_definition_id", nullable = false)
    private ItemDefinition itemDefinition;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "listing_fee", nullable = false)
    private int listingFee;

    @Column(name = "seller_fee_rate_bps", nullable = false)
    private int sellerFeeRateBps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionListingStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private long version = 0;

    public boolean isActiveAt(Instant now) {
        return status == AuctionListingStatus.ACTIVE
                && remainingQuantity > 0
                && expiresAt.isAfter(now);
    }
}
