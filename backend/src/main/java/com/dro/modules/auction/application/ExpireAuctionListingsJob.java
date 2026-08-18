package com.dro.modules.auction.application;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.infra.AuctionListingRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpireAuctionListingsJob {

    private static final int BATCH_SIZE = 100;
    private static final long RUN_INTERVAL_MILLIS = 60_000L;
    private static final Logger log = LoggerFactory.getLogger(ExpireAuctionListingsJob.class);

    private final AuctionListingRepository auctionListingRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final AuctionMailNotificationService auctionMailNotificationService;

    @Scheduled(fixedDelay = RUN_INTERVAL_MILLIS)
    @Transactional
    public void expireExpiredListings() {
        Instant now = Instant.now();
        var expiredIds = auctionListingRepository
                .findExpiredListingIds(now, PageRequest.of(0, BATCH_SIZE))
                .getContent();

        expiredIds.forEach(id -> expireListing(id, now));
    }

    private void expireListing(UUID listingId, Instant now) {
        AuctionListing listing = auctionListingRepository.findByIdForUpdate(listingId).orElse(null);
        if (listing == null || listing.getStatus() != AuctionListingStatus.ACTIVE
                || listing.getRemainingQuantity() <= 0 || listing.getExpiresAt().isAfter(now)) {
            return;
        }

        Digimon sourceDigimon = findSourceDigimon(listing);
        if (sourceDigimon == null) {
            log.warn("Could not expire auction listing {} because its source Digimon is unavailable", listingId);
            auctionMailNotificationService.notifyListingReturnPending(
                    listing,
                    "O Digimon de origem não está disponível no momento."
            );
            return;
        }

        ItemDefinition itemDefinition = listing.getItemDefinition();
        InventoryItem inventoryItem = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(sourceDigimon.getId(), itemDefinition.getId())
                .orElse(null);
        int currentQuantity = inventoryItem == null ? 0 : inventoryItem.getQuantity();
        long newQuantity = (long) currentQuantity + listing.getRemainingQuantity();
        if (newQuantity > Integer.MAX_VALUE
                || (itemDefinition.getMaxStack() != null && newQuantity > itemDefinition.getMaxStack())) {
            log.warn("Could not return expired auction listing {} because the inventory stack is full", listingId);
            auctionMailNotificationService.notifyListingReturnPending(
                    listing,
                    "O inventário não possui espaço suficiente para receber a devolução."
            );
            return;
        }

        int returnedQuantity = listing.getRemainingQuantity();
        saveInventory(inventoryItem, sourceDigimon, itemDefinition, (int) newQuantity);
        listing.setRemainingQuantity(0);
        listing.setStatus(AuctionListingStatus.EXPIRED);
        listing.setUpdatedAt(now);
        auctionListingRepository.save(listing);
        auctionMailNotificationService.notifyListingReturned(listing, returnedQuantity);
    }

    private Digimon findSourceDigimon(AuctionListing listing) {
        UUID sourceDigimonId = listing.getSellerDigimonId();
        if (sourceDigimonId == null) {
            Player seller = playerRepository.findById(listing.getSellerPlayerId()).orElse(null);
            sourceDigimonId = seller == null ? null : seller.getActiveDigimonId();
        }
        if (sourceDigimonId == null) {
            return null;
        }

        Digimon sourceDigimon = digimonRepository.findByIdForUpdate(sourceDigimonId).orElse(null);
        if (sourceDigimon == null || !sourceDigimon.getPlayerId().equals(listing.getSellerPlayerId())) {
            return null;
        }
        return sourceDigimon;
    }

    private void saveInventory(
            InventoryItem item,
            Digimon digimon,
            ItemDefinition definition,
            int quantity
    ) {
        if (item != null) {
            item.setQuantity(quantity);
            inventoryRepository.save(item);
            return;
        }

        inventoryRepository.save(InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimon.getId())
                .itemType(resolveItemType(definition.getCode()))
                .itemDefinition(definition)
                .quantity(quantity)
                .build());
    }

    private ItemType resolveItemType(String code) {
        try {
            return ItemType.valueOf(code);
        } catch (IllegalArgumentException exception) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }
}
