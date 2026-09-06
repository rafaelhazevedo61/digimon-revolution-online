package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.request.*;
import com.dro.modules.auction.domain.*;
import com.dro.modules.auction.infra.*;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.*;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.domain.*;
import com.dro.modules.inventory.infra.*;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.*;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Application-level regression tests. PostgreSQL race/rollback tests are documented separately. */
class AuctionEquipmentFlowTest {
    private PlayerRepository players;
    private DigimonRepository digimons;
    private ItemDefinitionRepository definitions;
    private InventoryRepository inventory;
    private AuctionListingRepository listings;
    private AuctionTransactionRepository transactions;
    private EquipmentRepository equipmentRepository;
    private AuctionMailNotificationService mail;
    private TransactionAuditPublisher audit;
    private AuctionEquipmentService custody;
    private Player seller;
    private Player buyer;
    private Digimon sellerDigimon;
    private Digimon buyerDigimon;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        players = mock(PlayerRepository.class);
        digimons = mock(DigimonRepository.class);
        definitions = mock(ItemDefinitionRepository.class);
        inventory = mock(InventoryRepository.class);
        listings = mock(AuctionListingRepository.class);
        transactions = mock(AuctionTransactionRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        mail = mock(AuctionMailNotificationService.class);
        audit = mock(TransactionAuditPublisher.class);
        custody = new AuctionEquipmentService(equipmentRepository);
        seller = player("seller");
        buyer = player("buyer");
        sellerDigimon = digimon(seller, 500);
        buyerDigimon = digimon(buyer, 5000);
        equipment = Equipment.builder().id(UUID.randomUUID()).playerId(seller.getId())
                .name("Overclock Blade").slot(EquipmentSlot.WEAPON).rarity(EquipmentRarity.LEGENDARY)
                .setCode("OVERCLOCK").tier(3).refinementLevel(7).ascensionLevel(2)
                .bonusHp(20).bonusAttack(40).bonusDefense(10).createdAt(LocalDateTime.now()).build();
        when(equipmentRepository.findByIdForUpdate(equipment.getId())).thenReturn(Optional.of(equipment));
        when(listings.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Player player(String username) {
        Player player = Player.builder().id(UUID.randomUUID()).username(username)
                .activeDigimonId(UUID.randomUUID()).build();
        when(players.findByIdForUpdate(player.getId())).thenReturn(Optional.of(player));
        when(players.findById(player.getId())).thenReturn(Optional.of(player));
        return player;
    }

    private Digimon digimon(Player player, int bits) {
        Digimon digimon = Digimon.builder().id(player.getActiveDigimonId()).playerId(player.getId()).bits(bits).build();
        when(digimons.findByIdForUpdate(digimon.getId())).thenReturn(Optional.of(digimon));
        return digimon;
    }

    private CreateAuctionListingUseCase creator() {
        return new CreateAuctionListingUseCase(players, digimons, definitions, inventory, listings, custody);
    }

    private BuyAuctionListingUseCase purchaser() {
        return new BuyAuctionListingUseCase(players, digimons, inventory, listings, transactions, mail, audit, custody);
    }

    private CreateAuctionListingRequest request() {
        return new CreateAuctionListingRequest(null, 1, 1000, 24, equipment.getId(), AuctionListingType.EQUIPMENT);
    }

    private AuctionListing published() {
        var response = creator().execute(JwtTestToken.create(seller.getId()), request());
        var capture = org.mockito.ArgumentCaptor.forClass(AuctionListing.class);
        verify(listings).save(capture.capture());
        AuctionListing listing = capture.getValue();
        assertEquals(response.id(), listing.getId());
        when(listings.findByIdForUpdate(listing.getId())).thenReturn(Optional.of(listing));
        return listing;
    }

    @Test
    void publicationReservesSameInstanceAndSnapshotsAllAttributes() {
        AuctionListing listing = published();
        assertEquals(EquipmentAvailability.AUCTION_ESCROW, equipment.getAvailability());
        assertEquals(400, sellerDigimon.getBits());
        assertEquals(equipment.getId(), listing.getEquipmentId());
        assertEquals(1, listing.getQuantity());
        assertNull(listing.getItemDefinition());
        assertEquals(7, listing.getEquipmentSnapshot().getRefinementLevel());
        assertEquals(2, listing.getEquipmentSnapshot().getAscensionLevel());
        assertEquals(40, listing.getEquipmentSnapshot().getBonusAttack());
        assertEquals(equipment.getEffectiveBonusAttack(), listing.getEquipmentSnapshot().getEffectiveBonusAttack());
        verifyNoInteractions(inventory, definitions);
    }

    @Test
    void purchaseTransfersSameInstanceAndKeepsSnapshotAfterLaterUpgrade() {
        AuctionListing listing = published();
        LocalDateTime createdAt = equipment.getCreatedAt();
        var result = purchaser().execute(JwtTestToken.create(buyer.getId()), listing.getId(), new BuyAuctionListingRequest(1));
        assertEquals(buyer.getId(), equipment.getPlayerId());
        assertEquals(EquipmentAvailability.AVAILABLE, equipment.getAvailability());
        assertNull(equipment.getDigimonId());
        assertFalse(equipment.isEquipped());
        assertEquals(createdAt, equipment.getCreatedAt());
        assertEquals(7, equipment.getRefinementLevel());
        assertEquals(2, equipment.getAscensionLevel());
        assertEquals(4000, buyerDigimon.getBits());
        assertEquals(1350, sellerDigimon.getBits());
        assertEquals(AuctionListingStatus.SOLD, listing.getStatus());
        assertEquals(0, listing.getRemainingQuantity());
        assertEquals(equipment.getId(), result.equipmentId());
        equipment.setRefinementLevel(8);
        assertEquals(7, result.equipment().getRefinementLevel());
        verify(transactions).save(any(AuctionTransaction.class));
        verify(mail).notifyPurchase(any(), eq("buyer"));
        verifyNoInteractions(inventory);
    }

    @Test
    void secondPurchaseIsRejected() {
        AuctionListing listing = published();
        purchaser().execute(JwtTestToken.create(buyer.getId()), listing.getId(), new BuyAuctionListingRequest(1));
        assertThrows(ConflictException.class, () -> purchaser().execute(JwtTestToken.create(buyer.getId()),
                listing.getId(), new BuyAuctionListingRequest(1)));
        assertEquals(4000, buyerDigimon.getBits());
        verify(transactions, times(1)).save(any());
    }

    @Test
    void cannotBuyOwnEquipmentOrMoreThanOne() {
        AuctionListing listing = published();
        assertThrows(BadRequestException.class, () -> purchaser().execute(JwtTestToken.create(seller.getId()),
                listing.getId(), new BuyAuctionListingRequest(1)));
        assertThrows(BadRequestException.class, () -> purchaser().execute(JwtTestToken.create(buyer.getId()),
                listing.getId(), new BuyAuctionListingRequest(2)));
        verifyNoInteractions(transactions);
    }

    @Test
    void insufficientBitsDoesNotDeliverOrCreateTransaction() {
        AuctionListing listing = published();
        buyerDigimon.setBits(0);
        assertThrows(UnprocessableException.class, () -> purchaser().execute(JwtTestToken.create(buyer.getId()),
                listing.getId(), new BuyAuctionListingRequest(1)));
        assertEquals(seller.getId(), equipment.getPlayerId());
        assertEquals(EquipmentAvailability.AUCTION_ESCROW, equipment.getAvailability());
        verifyNoInteractions(transactions);
    }

    @Test
    void cancelReturnsEquipmentWithoutSourceDigimonAndCannotReturnTwice() {
        AuctionListing listing = published();
        seller.setActiveDigimonId(null);
        clearInvocations(digimons);
        CancelAuctionListingUseCase cancel = new CancelAuctionListingUseCase(players, digimons, inventory, listings, mail, custody);
        cancel.execute(JwtTestToken.create(seller.getId()), listing.getId());
        assertEquals(seller.getId(), equipment.getPlayerId());
        assertEquals(EquipmentAvailability.AVAILABLE, equipment.getAvailability());
        assertEquals(AuctionListingStatus.CANCELLED, listing.getStatus());
        assertEquals(400, sellerDigimon.getBits()); // fee is not refunded
        assertThrows(ConflictException.class, () -> cancel.execute(JwtTestToken.create(seller.getId()), listing.getId()));
        assertThrows(ConflictException.class, () -> purchaser().execute(JwtTestToken.create(buyer.getId()),
                listing.getId(), new BuyAuctionListingRequest(1)));
        verifyNoInteractions(digimons, inventory);
    }

    @Test
    void expireReturnsEquipmentWithoutSourceDigimonAndIsIdempotent() {
        AuctionListing listing = published();
        listing.setExpiresAt(Instant.now().minusSeconds(1));
        seller.setActiveDigimonId(null);
        clearInvocations(digimons);
        when(listings.findExpiredListingIds(any(), any())).thenReturn(new PageImpl<>(List.of(listing.getId())));
        ExpireAuctionListingsJob job = new ExpireAuctionListingsJob(listings, players, digimons, inventory, mail, custody);
        job.expireExpiredListings();
        job.expireExpiredListings();
        assertEquals(AuctionListingStatus.EXPIRED, listing.getStatus());
        assertEquals(EquipmentAvailability.AVAILABLE, equipment.getAvailability());
        assertEquals(seller.getId(), equipment.getPlayerId());
        verify(mail, times(1)).notifyListingReturned(listing, 1);
        verifyNoInteractions(digimons, inventory);
    }

    @Test
    void publicationRejectsLockedEquippedForeignAndAlreadyReservedEquipment() {
        equipment.setLocked(true);
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        equipment.setLocked(false);
        equipment.setEquipped(true);
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        equipment.setEquipped(false);
        equipment.setDigimonId(sellerDigimon.getId());
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        equipment.setDigimonId(null);
        equipment.setPlayerId(buyer.getId());
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        equipment.setPlayerId(seller.getId());
        equipment.reserveForAuction(seller.getId());
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        assertEquals(500, sellerDigimon.getBits());
        verify(listings, never()).save(any());
    }

    @Test
    void publicationRespectsListingLimitAndFee() {
        when(listings.countActiveForSeller(eq(seller.getId()), any())).thenReturn(10L);
        assertThrows(ConflictException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        when(listings.countActiveForSeller(eq(seller.getId()), any())).thenReturn(0L);
        sellerDigimon.setBits(0);
        assertThrows(UnprocessableException.class, () -> creator().execute(JwtTestToken.create(seller.getId()), request()));
        assertEquals(EquipmentAvailability.AVAILABLE, equipment.getAvailability());
    }

    @Test
    void equipmentMailAndHistoryUseSnapshotNotItemDefinition() {
        AuctionListing listing = published();
        AuctionTransaction transaction = AuctionTransaction.builder().id(UUID.randomUUID()).listing(listing)
                .buyerPlayerId(buyer.getId()).sellerPlayerId(seller.getId()).quantity(1)
                .grossAmount(1000).fee(50).sellerNetAmount(950).build();
        assertTrue(AuctionMailNotificationFactory.purchaseForBuyer(transaction).body().contains("Overclock Blade"));
        assertTrue(AuctionMailNotificationFactory.purchaseForSeller(transaction).body().contains("Overclock Blade"));
        listing.setStatus(AuctionListingStatus.CANCELLED);
        assertTrue(AuctionMailNotificationFactory.listingReturned(listing, 1).body().contains("Overclock Blade"));
        equipment.setName("Upgraded Blade");
        assertEquals("Overclock Blade", transaction.getAssetName());
    }

    @Test
    void allEquipmentMutationPathsRejectEscrow() {
        published();
        String token = JwtTestToken.create(seller.getId());
        UUID id = equipment.getId();
        var equip = new com.dro.modules.equipment.application.EquipUseCase(equipmentRepository, digimons, players, null);
        var refine = new com.dro.modules.equipment.application.RefineEquipmentUseCase(equipmentRepository, digimons, players, inventory, audit, definitions);
        var ascend = new com.dro.modules.equipment.application.AscendEquipmentUseCase(equipmentRepository, digimons, players, inventory);
        var lock = new com.dro.modules.equipment.application.ToggleEquipmentLockUseCase(equipmentRepository);
        var dismantle = new com.dro.modules.equipment.application.DismantleEquipmentUseCase(equipmentRepository, definitions, null);
        var sell = new com.dro.modules.shop.application.SellShopProductUseCase(players, digimons, null, equipmentRepository, null, definitions, inventory);
        assertThrows(ConflictException.class, () -> equip.execute(token, id));
        assertThrows(ConflictException.class, () -> refine.execute(token, new com.dro.modules.equipment.api.dto.request.RefineEquipmentRequest(id, null, null)));
        assertThrows(ConflictException.class, () -> ascend.execute(token, new com.dro.modules.equipment.api.dto.request.AscendEquipmentRequest(id)));
        assertThrows(ConflictException.class, () -> lock.execute(token, new com.dro.modules.equipment.api.dto.request.ToggleEquipmentLockRequest(id, true)));
        assertThrows(ConflictException.class, () -> dismantle.executeBatch(token, List.of(id)));
        assertThrows(ConflictException.class, () -> sell.execute(token, new com.dro.modules.shop.api.dto.request.SellShopProductRequest(null, id, null, 1)));
        verify(equipmentRepository, never()).delete(any());
        verify(equipmentRepository, never()).deleteAllById(any());
        assertEquals(EquipmentAvailability.AUCTION_ESCROW, equipment.getAvailability());
    }

    @Test
    void enhancementRejectsEscrowForTargetAndMaterialCopies() {
        equipment.setTier(1);
        Equipment first = Equipment.builder().id(UUID.randomUUID()).playerId(seller.getId())
                .name(equipment.getName()).slot(equipment.getSlot()).rarity(equipment.getRarity())
                .setCode(equipment.getSetCode()).tier(1).build();
        Equipment second = Equipment.builder().id(UUID.randomUUID()).playerId(seller.getId())
                .name(equipment.getName()).slot(equipment.getSlot()).rarity(equipment.getRarity())
                .setCode(equipment.getSetCode()).tier(1).build();
        when(equipmentRepository.findByIdForUpdate(first.getId())).thenReturn(Optional.of(first));
        when(equipmentRepository.findByIdForUpdate(second.getId())).thenReturn(Optional.of(second));
        var enhancer = new com.dro.modules.equipment.application.EnhanceEquipmentUseCase(equipmentRepository, null, inventory, definitions);
        var request = new com.dro.modules.equipment.api.dto.request.EnhanceEquipmentRequest(equipment.getId(), List.of(first.getId(), second.getId()));
        equipment.reserveForAuction(seller.getId());
        assertThrows(ConflictException.class, () -> enhancer.execute(JwtTestToken.create(seller.getId()), request));
        equipment.releaseFromAuction(seller.getId(), seller.getId());
        first.reserveForAuction(seller.getId());
        assertThrows(ConflictException.class, () -> enhancer.execute(JwtTestToken.create(seller.getId()), request));
        verifyNoInteractions(inventory);
        verify(equipmentRepository, never()).deleteAllById(any());
    }

    @Test
    void changingTheSellerDoesNotAllowReservedEquipmentDelivery() {
        AuctionListing listing = published();
        equipment.setPlayerId(UUID.randomUUID());
        assertThrows(ConflictException.class, () -> custody.deliver(listing, buyer.getId()));
        assertEquals(EquipmentAvailability.AUCTION_ESCROW, equipment.getAvailability());
    }

    @Test
    void legacyItemPublicationAndPartialPurchaseStillUseInventory() {
        ItemDefinition item = ItemDefinition.builder().id(1L).code("REFINEMENT_STONE")
                .name("Stone").category("MATERIAL").rarity("COMMON").tradable(true).stackable(true).maxStack(999).build();
        InventoryItem stock = InventoryItem.builder().id(UUID.randomUUID()).playerId(seller.getId())
                .itemDefinition(item).quantity(10).build();
        when(definitions.findById(1L)).thenReturn(Optional.of(item));
        when(inventory.findByDigimonIdAndItemDefinitionIdForUpdate(sellerDigimon.getId(), 1L)).thenReturn(Optional.of(stock));
        creator().execute(JwtTestToken.create(seller.getId()), new CreateAuctionListingRequest(1L, 5, 100, 24));
        var capture = org.mockito.ArgumentCaptor.forClass(AuctionListing.class);
        verify(listings).save(capture.capture());
        AuctionListing listing = capture.getValue();
        assertEquals(5, stock.getQuantity());
        assertEquals(AuctionListingType.ITEM, listing.getListingType());
        when(listings.findByIdForUpdate(listing.getId())).thenReturn(Optional.of(listing));
        purchaser().execute(JwtTestToken.create(buyer.getId()), listing.getId(), new BuyAuctionListingRequest(2));
        assertEquals(3, listing.getRemainingQuantity());
        assertEquals(AuctionListingStatus.ACTIVE, listing.getStatus());
        verify(inventory, times(2)).save(any());
        verifyNoInteractions(equipmentRepository);
    }
}
