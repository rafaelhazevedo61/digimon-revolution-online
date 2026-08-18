package com.dro.modules.auction.application;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.auction.domain.AuctionListingStatus;
import com.dro.modules.auction.domain.AuctionTransaction;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public final class AuctionMailNotificationFactory {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private AuctionMailNotificationFactory() {
    }

    public static AuctionMailNotification purchaseForBuyer(AuctionTransaction transaction) {
        String itemName = transaction.getItemDefinition().getName();
        String quantity = format(transaction.getQuantity());
        String amount = format(transaction.getGrossAmount());
        return new AuctionMailNotification(
                transaction.getBuyerPlayerId(),
                transaction.getId(),
                "PURCHASE_COMPLETED_BUYER",
                "Compra realizada na Casa de Leilões",
                "Você comprou " + quantity + " unidade(s) de " + itemName
                        + " por " + amount + " Bits. O item já foi entregue ao seu Digimon.",
                "auction:transaction:" + transaction.getId() + ":buyer"
        );
    }

    public static AuctionMailNotification purchaseForSeller(AuctionTransaction transaction) {
        String itemName = transaction.getItemDefinition().getName();
        String quantity = format(transaction.getQuantity());
        String grossAmount = format(transaction.getGrossAmount());
        String fee = format(transaction.getFee());
        String netAmount = format(transaction.getSellerNetAmount());
        return new AuctionMailNotification(
                transaction.getSellerPlayerId(),
                transaction.getId(),
                "PURCHASE_COMPLETED_SELLER",
                "Venda concluída na Casa de Leilões",
                "Seu anúncio vendeu " + quantity + " unidade(s) de " + itemName
                        + ". Valor bruto: " + grossAmount + " Bits; comissão: " + fee
                        + " Bits; valor líquido recebido: " + netAmount + " Bits.",
                "auction:transaction:" + transaction.getId() + ":seller"
        );
    }

    public static AuctionMailNotification listingReturnPending(
            AuctionListing listing,
            String reason
    ) {
        String itemName = listing.getItemDefinition().getName();
        String quantity = format(listing.getRemainingQuantity());
        return new AuctionMailNotification(
            listing.getSellerPlayerId(),
            listing.getId(),
            "LISTING_RETURN_PENDING",
            "Devolução pendente na Casa de Leilões",
            "A devolução de " + quantity + " unidade(s) de " + itemName
                        + " está pendente. " + reason
                        + " O sistema tentará concluir a devolução automaticamente.",
                "auction:listing:" + listing.getId() + ":return-pending"
        );
    }

    public static AuctionMailNotification listingReturned(
            AuctionListing listing,
            int returnedQuantity
    ) {
        String itemName = listing.getItemDefinition().getName();
        String quantity = format(returnedQuantity);
        boolean expired = listing.getStatus() == AuctionListingStatus.EXPIRED;
        String actionType = expired ? "LISTING_EXPIRED" : "LISTING_CANCELLED";
        String subject = expired
                ? "Anúncio expirado na Casa de Leilões"
                : "Anúncio cancelado na Casa de Leilões";
        String reason = expired
                ? "O prazo do anúncio terminou"
                : "O anúncio foi cancelado";
        return new AuctionMailNotification(
                listing.getSellerPlayerId(),
                listing.getId(),
                actionType,
                subject,
                reason + ". " + quantity + " unidade(s) de " + itemName
                        + " foram devolvidas ao Digimon de origem.",
                "auction:listing:" + listing.getId() + ":" + (expired ? "expired" : "cancelled")
        );
    }

    private static String format(int value) {
        return NumberFormat.getIntegerInstance(PT_BR).format(value);
    }

    public record AuctionMailNotification(
            UUID recipientId,
            UUID sourceId,
            String actionType,
            String subject,
            String body,
            String deliveryKey
    ) {
    }
}
