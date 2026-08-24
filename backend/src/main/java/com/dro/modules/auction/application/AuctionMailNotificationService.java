package com.dro.modules.auction.application;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionTransaction;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente da camada de serviço de aplicação do módulo de Casa de Leilões.
 */
@Service
public class AuctionMailNotificationService {
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public void notifyPurchase(AuctionTransaction transaction) {
        notifyMessage(AuctionMailNotificationFactory.purchaseForBuyer(transaction));
        notifyMessage(AuctionMailNotificationFactory.purchaseForSeller(transaction));
    }

    @Transactional
    public void notifyListingReturned(AuctionListing listing, int returnedQuantity) {
        notifyMessage(AuctionMailNotificationFactory.listingReturned(listing, returnedQuantity));
    }

    @Transactional
    public void notifyListingReturnPending(AuctionListing listing, String reason) {
        notifyMessage(AuctionMailNotificationFactory.listingReturnPending(listing, reason));
    }

    private void notifyMessage(AuctionMailNotificationFactory.AuctionMailNotification notification) {
        createSystemMailMessageUseCase.createAuctionNotification(notification.recipientId(), notification.sourceId(), notification.actionType(), notification.subject(), notification.body(), notification.deliveryKey());
    }

    public AuctionMailNotificationService(final CreateSystemMailMessageUseCase createSystemMailMessageUseCase) {
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
    }
}
