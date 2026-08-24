package com.dro.modules.auction.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Componente da camada de modelo de domínio do módulo de Casa de Leilões.
 */
@Entity
@Table(name = "auction_transactions")
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


    public static class AuctionTransactionBuilder {
        private UUID id;
        private AuctionListing listing;
        private UUID sellerPlayerId;
        private UUID buyerPlayerId;
        private ItemDefinition itemDefinition;
        private int quantity;
        private int unitPrice;
        private int grossAmount;
        private int fee;
        private int sellerNetAmount;
        private Instant createdAt;

        AuctionTransactionBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder listing(final AuctionListing listing) {
            this.listing = listing;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder sellerPlayerId(final UUID sellerPlayerId) {
            this.sellerPlayerId = sellerPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder buyerPlayerId(final UUID buyerPlayerId) {
            this.buyerPlayerId = buyerPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder itemDefinition(final ItemDefinition itemDefinition) {
            this.itemDefinition = itemDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder unitPrice(final int unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder grossAmount(final int grossAmount) {
            this.grossAmount = grossAmount;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder fee(final int fee) {
            this.fee = fee;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder sellerNetAmount(final int sellerNetAmount) {
            this.sellerNetAmount = sellerNetAmount;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionTransaction.AuctionTransactionBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AuctionTransaction build() {
            return new AuctionTransaction(this.id, this.listing, this.sellerPlayerId, this.buyerPlayerId, this.itemDefinition, this.quantity, this.unitPrice, this.grossAmount, this.fee, this.sellerNetAmount, this.createdAt);
        }

        @Override
        public String toString() {
            return "AuctionTransaction.AuctionTransactionBuilder(id=" + this.id + ", listing=" + this.listing + ", sellerPlayerId=" + this.sellerPlayerId + ", buyerPlayerId=" + this.buyerPlayerId + ", itemDefinition=" + this.itemDefinition + ", quantity=" + this.quantity + ", unitPrice=" + this.unitPrice + ", grossAmount=" + this.grossAmount + ", fee=" + this.fee + ", sellerNetAmount=" + this.sellerNetAmount + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static AuctionTransaction.AuctionTransactionBuilder builder() {
        return new AuctionTransaction.AuctionTransactionBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public AuctionListing getListing() {
        return this.listing;
    }

    public UUID getSellerPlayerId() {
        return this.sellerPlayerId;
    }

    public UUID getBuyerPlayerId() {
        return this.buyerPlayerId;
    }

    public ItemDefinition getItemDefinition() {
        return this.itemDefinition;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getUnitPrice() {
        return this.unitPrice;
    }

    public int getGrossAmount() {
        return this.grossAmount;
    }

    public int getFee() {
        return this.fee;
    }

    public int getSellerNetAmount() {
        return this.sellerNetAmount;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setListing(final AuctionListing listing) {
        this.listing = listing;
    }

    public void setSellerPlayerId(final UUID sellerPlayerId) {
        this.sellerPlayerId = sellerPlayerId;
    }

    public void setBuyerPlayerId(final UUID buyerPlayerId) {
        this.buyerPlayerId = buyerPlayerId;
    }

    public void setItemDefinition(final ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(final int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setGrossAmount(final int grossAmount) {
        this.grossAmount = grossAmount;
    }

    public void setFee(final int fee) {
        this.fee = fee;
    }

    public void setSellerNetAmount(final int sellerNetAmount) {
        this.sellerNetAmount = sellerNetAmount;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public AuctionTransaction() {
    }

    public AuctionTransaction(final UUID id, final AuctionListing listing, final UUID sellerPlayerId, final UUID buyerPlayerId, final ItemDefinition itemDefinition, final int quantity, final int unitPrice, final int grossAmount, final int fee, final int sellerNetAmount, final Instant createdAt) {
        this.id = id;
        this.listing = listing;
        this.sellerPlayerId = sellerPlayerId;
        this.buyerPlayerId = buyerPlayerId;
        this.itemDefinition = itemDefinition;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.grossAmount = grossAmount;
        this.fee = fee;
        this.sellerNetAmount = sellerNetAmount;
        this.createdAt = createdAt;
    }
}
