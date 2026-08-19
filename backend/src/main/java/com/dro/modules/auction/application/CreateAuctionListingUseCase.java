package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.request.CreateAuctionListingRequest;
import com.dro.modules.auction.api.dto.response.AuctionListingResponse;
import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingMapper;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.domain.AuctionRules;
import com.dro.modules.auction.infra.AuctionListingRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
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

/**
 * Componente da camada de caso de uso da aplicação do módulo de Casa de Leilões.
 */
@Service
@RequiredArgsConstructor
public class CreateAuctionListingUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final InventoryRepository inventoryRepository;
    private final AuctionListingRepository auctionListingRepository;

    @Transactional
    public AuctionListingResponse execute(String token, CreateAuctionListingRequest request) {
        AuctionRules.validateListing(request.quantity(), request.unitPrice(), request.durationHours());

        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        Instant now = Instant.now();
        if (auctionListingRepository.countActiveForSeller(playerId, now)
                >= AuctionRules.MAX_ACTIVE_LISTINGS_PER_PLAYER) {
            throw new ConflictException("Você já possui o limite de 10 anúncios ativos.");
        }

        ItemDefinition itemDefinition = itemDefinitionRepository.findById(request.itemDefinitionId())
                .orElseThrow(() -> new NotFoundException("Item definition not found"));

        if (!itemDefinition.isTradable()) {
            throw new UnprocessableException("This item cannot be traded");
        }
        if (!itemDefinition.isStackable()) {
            throw new UnprocessableException("Only stackable items can be listed in the auction house");
        }

        Digimon sellerDigimon = findLockedActiveDigimon(player, playerId);
        InventoryItem inventoryItem = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(sellerDigimon.getId(), itemDefinition.getId())
                .orElseThrow(() -> new NotFoundException("Item not found in active Digimon inventory"));

        if (inventoryItem.getQuantity() < request.quantity()) {
            throw new BadRequestException(
                    "Not enough items. You have " + inventoryItem.getQuantity());
        }
        if (sellerDigimon.getBits() < AuctionRules.LISTING_FEE) {
            throw new UnprocessableException("Not enough Bits for the listing fee");
        }

        inventoryItem.setQuantity(inventoryItem.getQuantity() - request.quantity());
        if (inventoryItem.getQuantity() == 0) {
            inventoryRepository.delete(inventoryItem);
        } else {
            inventoryRepository.save(inventoryItem);
        }

        sellerDigimon.setBits(sellerDigimon.getBits() - AuctionRules.LISTING_FEE);
        digimonRepository.save(sellerDigimon);

        int sellerFeeRateBps = AuctionRules.sellerFeeRateBpsForDuration(request.durationHours());
        AuctionListing listing = AuctionListing.builder()
                .id(UUID.randomUUID())
                .sellerPlayerId(playerId)
                .sellerDigimonId(sellerDigimon.getId())
                .itemDefinition(itemDefinition)
                .quantity(request.quantity())
                .remainingQuantity(request.quantity())
                .unitPrice(request.unitPrice())
                .listingFee(AuctionRules.LISTING_FEE)
                .sellerFeeRateBps(sellerFeeRateBps)
                .status(AuctionListingStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(AuctionRules.expirationAt(now, request.durationHours()))
                .updatedAt(now)
                .build();

        AuctionListing saved = auctionListingRepository.save(listing);
        return AuctionListingMapper.toResponse(saved, player.getUsername());
    }

    private Digimon findLockedActiveDigimon(Player player, UUID playerId) {
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active Digimon selected");
        }

        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("Active Digimon does not belong to player");
        }
        return digimon;
    }
}
