package com.dro.modules.auction.infra;

import com.dro.modules.auction.domain.AuctionTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction, UUID> {

    Page<AuctionTransaction> findByBuyerPlayerIdOrSellerPlayerIdOrderByCreatedAtDesc(
            UUID buyerPlayerId,
            UUID sellerPlayerId,
            Pageable pageable
    );
}
