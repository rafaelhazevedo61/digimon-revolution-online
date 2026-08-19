package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.response.AuctionListingResponse;
import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingMapper;
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
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelAuctionListingUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final AuctionListingRepository auctionListingRepository;
    private final AuctionMailNotificationService auctionMailNotificationService;

    @Transactional
    public AuctionListingResponse execute(String token, UUID listingId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        AuctionListing listing = auctionListingRepository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new NotFoundException("Auction listing not found"));

        if (!listing.getSellerPlayerId().equals(playerId)) {
            throw new ForbiddenException("Only the seller can cancel this listing");
        }
        if (listing.getStatus() != AuctionListingStatus.ACTIVE || listing.getRemainingQuantity() <= 0) {
            throw new ConflictException("Auction listing is no longer active");
        }

        Digimon sellerDigimon = findLockedSellerDigimon(player, listing);
        ItemDefinition itemDefinition = listing.getItemDefinition();
        InventoryItem inventoryItem = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(
                        sellerDigimon.getId(),
                        itemDefinition.getId()
                )
                .orElse(null);

        int currentQuantity = inventoryItem == null ? 0 : inventoryItem.getQuantity();
        int returnedQuantity = listing.getRemainingQuantity();
        int newQuantity = currentQuantity + returnedQuantity;
        if (itemDefinition.getMaxStack() != null
                && newQuantity > itemDefinition.getMaxStack()) {
            throw new UnprocessableException(
                    "Cannot return items because the inventory stack would exceed "
                            + itemDefinition.getMaxStack());
        }

        saveInventory(inventoryItem, sellerDigimon, itemDefinition, newQuantity);
        Instant now = Instant.now();
        listing.setRemainingQuantity(0);
        listing.setStatus(listing.getExpiresAt().isAfter(now)
                ? AuctionListingStatus.CANCELLED
                : AuctionListingStatus.EXPIRED);
        listing.setUpdatedAt(now);
        auctionListingRepository.save(listing);
        auctionMailNotificationService.notifyListingReturned(listing, returnedQuantity);

        return AuctionListingMapper.toResponse(listing, player.getUsername());
    }

    private Digimon findLockedSellerDigimon(Player player, AuctionListing listing) {
        if (listing.getSellerDigimonId() != null) {
            Digimon sellerDigimon = digimonRepository.findByIdForUpdate(listing.getSellerDigimonId())
                    .orElseThrow(() -> new NotFoundException("Source Digimon not found"));
            if (!sellerDigimon.getPlayerId().equals(player.getId())) {
                throw new ForbiddenException("Source Digimon does not belong to seller");
            }
            return sellerDigimon;
        }
        return findLockedActiveDigimon(player);
    }

    private Digimon findLockedActiveDigimon(Player player) {
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active Digimon selected");
        }
        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));
        if (!digimon.getPlayerId().equals(player.getId())) {
            throw new ForbiddenException("Active Digimon does not belong to player");
        }
        return digimon;
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
