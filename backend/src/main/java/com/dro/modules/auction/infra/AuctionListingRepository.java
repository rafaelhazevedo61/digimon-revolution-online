package com.dro.modules.auction.infra;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Casa de Leilões.
 */
public interface AuctionListingRepository extends JpaRepository<AuctionListing, UUID> {

    @Query(
            value = """
                    SELECT listing FROM AuctionListing listing
                    LEFT JOIN FETCH listing.itemDefinition item
                    WHERE listing.status = :status
                      AND listing.remainingQuantity > 0
                      AND listing.expiresAt > :now
                      AND (:search = '' OR LOWER(COALESCE(item.name, listing.equipmentSnapshot.name)) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(item.code, listing.equipmentSnapshot.setCode, '')) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:category = '' OR item.category = :category OR (:category = 'EQUIPMENT' AND listing.listingType = com.dro.modules.auction.domain.AuctionListingType.EQUIPMENT))
                      AND (:rarity = '' OR COALESCE(item.rarity, listing.equipmentSnapshot.rarity) = :rarity)
                    """,
            countQuery = """
                    SELECT COUNT(listing) FROM AuctionListing listing
                    LEFT JOIN listing.itemDefinition item
                    WHERE listing.status = :status
                      AND listing.remainingQuantity > 0
                      AND listing.expiresAt > :now
                      AND (:search = '' OR LOWER(COALESCE(item.name, listing.equipmentSnapshot.name)) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(item.code, listing.equipmentSnapshot.setCode, '')) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:category = '' OR item.category = :category OR (:category = 'EQUIPMENT' AND listing.listingType = com.dro.modules.auction.domain.AuctionListingType.EQUIPMENT))
                      AND (:rarity = '' OR COALESCE(item.rarity, listing.equipmentSnapshot.rarity) = :rarity)
                    """
    )
    Page<AuctionListing> searchActive(
            @Param("status") AuctionListingStatus status,
            @Param("now") Instant now,
            @Param("search") String search,
            @Param("category") String category,
            @Param("rarity") String rarity,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT listing FROM AuctionListing listing WHERE listing.id = :id")
    Optional<AuctionListing> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            SELECT COUNT(listing) FROM AuctionListing listing
            WHERE listing.sellerPlayerId = :sellerPlayerId
              AND listing.status = com.dro.modules.auction.domain.AuctionListingStatus.ACTIVE
              AND listing.remainingQuantity > 0
              AND listing.expiresAt > :now
            """)
    long countActiveForSeller(
            @Param("sellerPlayerId") UUID sellerPlayerId,
            @Param("now") Instant now
    );

    @Query(
            value = """
                    SELECT listing FROM AuctionListing listing
                    LEFT JOIN FETCH listing.itemDefinition item
                    WHERE listing.sellerPlayerId = :sellerPlayerId
                    ORDER BY listing.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(listing) FROM AuctionListing listing
                    WHERE listing.sellerPlayerId = :sellerPlayerId
                    """
    )
    Page<AuctionListing> findSellerListings(
            @Param("sellerPlayerId") UUID sellerPlayerId,
            Pageable pageable
    );

    @Query("""
            SELECT listing.id FROM AuctionListing listing
            WHERE listing.status = com.dro.modules.auction.domain.AuctionListingStatus.ACTIVE
              AND listing.remainingQuantity > 0
              AND listing.expiresAt <= :now
            ORDER BY listing.expiresAt ASC
            """)
    Page<UUID> findExpiredListingIds(@Param("now") Instant now, Pageable pageable);
}
