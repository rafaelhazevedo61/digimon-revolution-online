package com.dro.modules.auction.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de modelo de domínio do módulo de Casa de Leilões.
 */
@Entity
@Table(name = "auction_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionTransaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private AuctionListing listing;

    @Column(name = "seller_player_id", nullable = false)
    private UUID sellerPlayerId;

    @Column(name = "buyer_player_id", nullable = false)
    private UUID buyerPlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_definition_id", nullable = false)
    private ItemDefinition itemDefinition;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(nullable = false)
    private int fee;

    @Column(name = "seller_net_amount", nullable = false)
    private int sellerNetAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
