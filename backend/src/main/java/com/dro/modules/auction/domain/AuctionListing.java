package com.dro.modules.auction.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Anúncio de um item empilhável publicado para compra imediata.
 *
 * <p>O anúncio reserva {@code remainingQuantity} unidades do item e mantém o
 * Digimon de origem para que cancelamentos e expirações devolvam o estoque ao
 * proprietário correto. A versão otimista ajuda a detectar atualizações
 * concorrentes do mesmo anúncio.</p>
 */
@Entity
@Table(name = "auction_listings")
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
    private long version;

    /**
     * Verifica se o anúncio ainda pode receber uma compra no instante informado.
     *
     * @param now instante atual usado na validação
     * @return {@code true} somente para anúncio ativo, não esgotado e não expirado
     */
    public boolean isActiveAt(Instant now) {
        return status == AuctionListingStatus.ACTIVE && remainingQuantity > 0 && expiresAt.isAfter(now);
    }

    private static long $default$version() {
        return 0;
    }


    public static class AuctionListingBuilder {
        private UUID id;
        private UUID sellerPlayerId;
        private UUID sellerDigimonId;
        private ItemDefinition itemDefinition;
        private int quantity;
        private int remainingQuantity;
        private int unitPrice;
        private int listingFee;
        private int sellerFeeRateBps;
        private AuctionListingStatus status;
        private Instant createdAt;
        private Instant expiresAt;
        private Instant updatedAt;
        private boolean version$set;
        private long version$value;

        AuctionListingBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder sellerPlayerId(final UUID sellerPlayerId) {
            this.sellerPlayerId = sellerPlayerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder sellerDigimonId(final UUID sellerDigimonId) {
            this.sellerDigimonId = sellerDigimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder itemDefinition(final ItemDefinition itemDefinition) {
            this.itemDefinition = itemDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder remainingQuantity(final int remainingQuantity) {
            this.remainingQuantity = remainingQuantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder unitPrice(final int unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder listingFee(final int listingFee) {
            this.listingFee = listingFee;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder sellerFeeRateBps(final int sellerFeeRateBps) {
            this.sellerFeeRateBps = sellerFeeRateBps;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder status(final AuctionListingStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder expiresAt(final Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AuctionListing.AuctionListingBuilder version(final long version) {
            this.version$value = version;
            version$set = true;
            return this;
        }

        public AuctionListing build() {
            long version$value = this.version$value;
            if (!this.version$set) version$value = AuctionListing.$default$version();
            return new AuctionListing(this.id, this.sellerPlayerId, this.sellerDigimonId, this.itemDefinition, this.quantity, this.remainingQuantity, this.unitPrice, this.listingFee, this.sellerFeeRateBps, this.status, this.createdAt, this.expiresAt, this.updatedAt, version$value);
        }

        @Override
        public String toString() {
            return "AuctionListing.AuctionListingBuilder(id=" + this.id + ", sellerPlayerId=" + this.sellerPlayerId + ", sellerDigimonId=" + this.sellerDigimonId + ", itemDefinition=" + this.itemDefinition + ", quantity=" + this.quantity + ", remainingQuantity=" + this.remainingQuantity + ", unitPrice=" + this.unitPrice + ", listingFee=" + this.listingFee + ", sellerFeeRateBps=" + this.sellerFeeRateBps + ", status=" + this.status + ", createdAt=" + this.createdAt + ", expiresAt=" + this.expiresAt + ", updatedAt=" + this.updatedAt + ", version$value=" + this.version$value + ")";
        }
    }

    public static AuctionListing.AuctionListingBuilder builder() {
        return new AuctionListing.AuctionListingBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getSellerPlayerId() {
        return this.sellerPlayerId;
    }

    public UUID getSellerDigimonId() {
        return this.sellerDigimonId;
    }

    public ItemDefinition getItemDefinition() {
        return this.itemDefinition;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getRemainingQuantity() {
        return this.remainingQuantity;
    }

    public int getUnitPrice() {
        return this.unitPrice;
    }

    public int getListingFee() {
        return this.listingFee;
    }

    public int getSellerFeeRateBps() {
        return this.sellerFeeRateBps;
    }

    public AuctionListingStatus getStatus() {
        return this.status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public long getVersion() {
        return this.version;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setSellerPlayerId(final UUID sellerPlayerId) {
        this.sellerPlayerId = sellerPlayerId;
    }

    public void setSellerDigimonId(final UUID sellerDigimonId) {
        this.sellerDigimonId = sellerDigimonId;
    }

    public void setItemDefinition(final ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setRemainingQuantity(final int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public void setUnitPrice(final int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setListingFee(final int listingFee) {
        this.listingFee = listingFee;
    }

    public void setSellerFeeRateBps(final int sellerFeeRateBps) {
        this.sellerFeeRateBps = sellerFeeRateBps;
    }

    public void setStatus(final AuctionListingStatus status) {
        this.status = status;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVersion(final long version) {
        this.version = version;
    }

    public AuctionListing() {
        this.version = AuctionListing.$default$version();
    }

    public AuctionListing(final UUID id, final UUID sellerPlayerId, final UUID sellerDigimonId, final ItemDefinition itemDefinition, final int quantity, final int remainingQuantity, final int unitPrice, final int listingFee, final int sellerFeeRateBps, final AuctionListingStatus status, final Instant createdAt, final Instant expiresAt, final Instant updatedAt, final long version) {
        this.id = id;
        this.sellerPlayerId = sellerPlayerId;
        this.sellerDigimonId = sellerDigimonId;
        this.itemDefinition = itemDefinition;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.unitPrice = unitPrice;
        this.listingFee = listingFee;
        this.sellerFeeRateBps = sellerFeeRateBps;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }
}
