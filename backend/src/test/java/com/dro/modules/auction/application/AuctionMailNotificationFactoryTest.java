package com.dro.modules.auction.application;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.domain.AuctionTransaction;
import com.dro.modules.inventory.domain.ItemDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionMailNotificationFactoryTest {

    @Test
    void purchaseNotifications_haveDifferentRecipientsAndStableDeliveryKeys() {
        UUID transactionId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        AuctionTransaction transaction = AuctionTransaction.builder()
                .id(transactionId)
                .buyerPlayerId(buyerId)
                .sellerPlayerId(sellerId)
                .itemDefinition(itemDefinition("Refinement Stone"))
                .quantity(2)
                .grossAmount(1500)
                .fee(75)
                .sellerNetAmount(1425)
                .build();

        var buyerMessage = AuctionMailNotificationFactory.purchaseForBuyer(transaction);
        var sellerMessage = AuctionMailNotificationFactory.purchaseForSeller(transaction);

        assertEquals(buyerId, buyerMessage.recipientId());
        assertEquals(sellerId, sellerMessage.recipientId());
        assertEquals("auction:transaction:" + transactionId + ":buyer", buyerMessage.deliveryKey());
        assertEquals("auction:transaction:" + transactionId + ":seller", sellerMessage.deliveryKey());
        assertTrue(buyerMessage.body().contains("1.500 Bits"));
        assertTrue(sellerMessage.body().contains("1.425 Bits"));
        assertTrue(sellerMessage.body().contains("75 Bits"));
    }

    @Test
    void returnedListingNotifications_useDistinctKeysForCancelAndExpiration() {
        UUID listingId = UUID.randomUUID();
        AuctionListing listing = AuctionListing.builder()
                .id(listingId)
                .sellerPlayerId(UUID.randomUUID())
                .itemDefinition(itemDefinition("Data Core"))
                .remainingQuantity(3)
                .status(AuctionListingStatus.CANCELLED)
                .build();

        var cancelled = AuctionMailNotificationFactory.listingReturned(listing, 3);
        listing.setStatus(AuctionListingStatus.EXPIRED);
        var expired = AuctionMailNotificationFactory.listingReturned(listing, 3);

        assertEquals("LISTING_CANCELLED", cancelled.actionType());
        assertEquals("auction:listing:" + listingId + ":cancelled", cancelled.deliveryKey());
        assertEquals("LISTING_EXPIRED", expired.actionType());
        assertEquals("auction:listing:" + listingId + ":expired", expired.deliveryKey());
    }

    @Test
    void pendingReturnNotification_hasOneStableDeliveryKey() {
        UUID listingId = UUID.randomUUID();
        AuctionListing listing = AuctionListing.builder()
                .id(listingId)
                .sellerPlayerId(UUID.randomUUID())
                .itemDefinition(itemDefinition("Digitama"))
                .remainingQuantity(1)
                .status(AuctionListingStatus.ACTIVE)
                .build();

        var first = AuctionMailNotificationFactory.listingReturnPending(listing, "Espaço insuficiente.");
        var second = AuctionMailNotificationFactory.listingReturnPending(listing, "O Digimon está indisponível.");

        assertEquals("LISTING_RETURN_PENDING", first.actionType());
        assertEquals(first.deliveryKey(), second.deliveryKey());
        assertTrue(first.body().contains("Espaço insuficiente."));
    }

    private ItemDefinition itemDefinition(String name) {
        return ItemDefinition.builder()
                .id(1L)
                .code(name.toLowerCase().replace(" ", "_"))
                .name(name)
                .build();
    }
}
