package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.request.BuyAuctionListingRequest;
import com.dro.modules.auction.api.dto.response.AuctionPurchaseResponse;
import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.domain.AuctionRules;
import com.dro.modules.auction.domain.AuctionTransaction;
import com.dro.modules.auction.infra.AuctionListingRepository;
import com.dro.modules.auction.infra.AuctionTransactionRepository;
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
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Executa compras imediatas e parciais na Casa de Leilões.
 *
 * <p>A operação bloqueia o anúncio, os Digimons ativos em ordem estável e o
 * estoque do item antes de transferir Bits e unidades. Tudo é persistido na
 * mesma transação, incluindo a transação histórica e as notificações do Correio.</p>
 */
@Service
@RequiredArgsConstructor
public class BuyAuctionListingUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final AuctionListingRepository auctionListingRepository;
    private final AuctionTransactionRepository auctionTransactionRepository;
    private final AuctionMailNotificationService auctionMailNotificationService;

    /**
     * Compra uma quantidade de unidades de um anúncio ativo.
     *
     * @param token token JWT do comprador
     * @param listingId anúncio que será comprado
     * @param request quantidade desejada
     * @return resumo da compra, comissão, saldo e estoque restante
     * @throws BadRequestException quando a quantidade é inválida ou o comprador
     *                             tenta comprar o próprio anúncio
     * @throws ConflictException quando o anúncio, estoque ou ownership mudou
     * @throws UnprocessableException quando faltam Bits ou espaço na pilha
     */
    @Transactional
    public AuctionPurchaseResponse execute(
            String token,
            UUID listingId,
            BuyAuctionListingRequest request
    ) {
        if (request.quantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        UUID buyerPlayerId = TokenExtractor.extractPlayerId(token);
        Player buyer = playerRepository.findById(buyerPlayerId)
                .orElseThrow(() -> new ConflictException("Buyer player not found"));

        AuctionListing listing = auctionListingRepository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new ConflictException("Auction listing is no longer available"));

        Instant now = Instant.now();
        if (!listing.isActiveAt(now)) {
            throw new ConflictException("Auction listing is expired or no longer available");
        }
        if (listing.getSellerPlayerId().equals(buyerPlayerId)) {
            throw new BadRequestException("You cannot buy your own auction listing");
        }
        if (request.quantity() > listing.getRemainingQuantity()) {
            throw new ConflictException(
                    "Only " + listing.getRemainingQuantity() + " item(s) remain in this listing");
        }

        Player seller = playerRepository.findById(listing.getSellerPlayerId())
                .orElseThrow(() -> new ConflictException("Seller player not found"));
        LockedDigimons lockedDigimons = lockDigimonsInStableOrder(buyer, seller, buyerPlayerId);
        Digimon buyerDigimon = lockedDigimons.buyer();
        Digimon sellerDigimon = lockedDigimons.seller();

        ItemDefinition itemDefinition = listing.getItemDefinition();
        int grossAmount = AuctionRules.calculateGrossAmount(request.quantity(), listing.getUnitPrice());
        int sellerFee = AuctionRules.calculateSellerFee(grossAmount, listing.getSellerFeeRateBps());
        int sellerNetAmount = grossAmount - sellerFee;

        if (buyerDigimon.getBits() < grossAmount) {
            throw new UnprocessableException("Not enough Bits");
        }

        InventoryItem buyerInventory = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(
                        buyerDigimon.getId(),
                        itemDefinition.getId()
                )
                .orElse(null);
        int currentQuantity = buyerInventory == null ? 0 : buyerInventory.getQuantity();
        int newQuantity = currentQuantity + request.quantity();
        if (itemDefinition.getMaxStack() != null
                && newQuantity > itemDefinition.getMaxStack()) {
            throw new UnprocessableException(
                    "Cannot exceed max stack of " + itemDefinition.getMaxStack()
                            + " for item " + itemDefinition.getCode());
        }

        buyerDigimon.setBits(buyerDigimon.getBits() - grossAmount);
        sellerDigimon.setBits(sellerDigimon.getBits() + sellerNetAmount);
        saveBuyerInventory(buyerInventory, buyerDigimon, itemDefinition, newQuantity);

        listing.setRemainingQuantity(listing.getRemainingQuantity() - request.quantity());
        listing.setStatus(listing.getRemainingQuantity() == 0
                ? AuctionListingStatus.SOLD
                : AuctionListingStatus.ACTIVE);
        listing.setUpdatedAt(now);

        AuctionTransaction transaction = AuctionTransaction.builder()
                .id(UUID.randomUUID())
                .listing(listing)
                .sellerPlayerId(listing.getSellerPlayerId())
                .buyerPlayerId(buyerPlayerId)
                .itemDefinition(itemDefinition)
                .quantity(request.quantity())
                .unitPrice(listing.getUnitPrice())
                .grossAmount(grossAmount)
                .fee(sellerFee)
                .sellerNetAmount(sellerNetAmount)
                .createdAt(now)
                .build();

        digimonRepository.save(buyerDigimon);
        digimonRepository.save(sellerDigimon);
        auctionListingRepository.save(listing);
        auctionTransactionRepository.save(transaction);
        auctionMailNotificationService.notifyPurchase(transaction);

        return new AuctionPurchaseResponse(
                listing.getId(),
                itemDefinition.getCode(),
                itemDefinition.getName(),
                request.quantity(),
                grossAmount,
                sellerFee,
                grossAmount,
                sellerNetAmount,
                listing.getRemainingQuantity(),
                listing.getStatus(),
                buyerDigimon.getBits(),
                "Compra realizada com sucesso!"
        );
    }

    /**
     * Bloqueia os dois Digimons em ordem de UUID para reduzir deadlocks concorrentes.
     */
    private LockedDigimons lockDigimonsInStableOrder(
            Player buyer,
            Player seller,
            UUID buyerPlayerId
    ) {
        if (buyer.getActiveDigimonId() == null || seller.getActiveDigimonId() == null) {
            throw new ConflictException("A player in this transaction has no active Digimon");
        }

        UUID firstId = buyer.getActiveDigimonId().compareTo(seller.getActiveDigimonId()) < 0
                ? buyer.getActiveDigimonId()
                : seller.getActiveDigimonId();
        UUID secondId = firstId.equals(buyer.getActiveDigimonId())
                ? seller.getActiveDigimonId()
                : buyer.getActiveDigimonId();

        Digimon first = findLockedDigimon(firstId, buyer, seller, buyerPlayerId);
        Digimon second = findLockedDigimon(secondId, buyer, seller, buyerPlayerId);
        Digimon buyerDigimon = first.getId().equals(buyer.getActiveDigimonId()) ? first : second;
        Digimon sellerDigimon = first.getId().equals(seller.getActiveDigimonId()) ? first : second;
        return new LockedDigimons(buyerDigimon, sellerDigimon);
    }

    private Digimon findLockedDigimon(
            UUID digimonId,
            Player buyer,
            Player seller,
            UUID buyerPlayerId
    ) {
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId)
                .orElseThrow(() -> new ConflictException("Active Digimon not found"));
        boolean belongsToBuyer = buyer.getId().equals(buyerPlayerId)
                && digimon.getPlayerId().equals(buyer.getId());
        boolean belongsToSeller = digimon.getPlayerId().equals(seller.getId());
        if (!belongsToBuyer && !belongsToSeller) {
            throw new ConflictException("Digimon ownership changed during the purchase");
        }
        return digimon;
    }

    private record LockedDigimons(Digimon buyer, Digimon seller) {
    }

    private void saveBuyerInventory(
            InventoryItem inventoryItem,
            Digimon buyerDigimon,
            ItemDefinition itemDefinition,
            int newQuantity
    ) {
        if (inventoryItem != null) {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
            return;
        }

        InventoryItem created = InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(buyerDigimon.getId())
                .itemType(resolveItemType(itemDefinition.getCode()))
                .itemDefinition(itemDefinition)
                .quantity(newQuantity)
                .build();
        inventoryRepository.save(created);
    }

    private ItemType resolveItemType(String code) {
        try {
            return ItemType.valueOf(code);
        } catch (IllegalArgumentException exception) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }

}
