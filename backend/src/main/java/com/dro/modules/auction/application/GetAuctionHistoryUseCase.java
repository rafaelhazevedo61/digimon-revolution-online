package com.dro.modules.auction.application;

import com.dro.modules.auction.api.dto.response.AuctionTransactionResponse;
import com.dro.modules.auction.infra.AuctionTransactionRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Casa de Leilões.
 */
@Service
public class GetAuctionHistoryUseCase {
    private static final int MAX_PAGE_SIZE = 50;
    private final AuctionTransactionRepository auctionTransactionRepository;
    private final PlayerRepository playerRepository;

    public java.util.List<AuctionTransactionResponse> execute(String token, int page, int size) {
        var playerId = TokenExtractor.extractPlayerId(token);
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        return auctionTransactionRepository.findByBuyerPlayerIdOrSellerPlayerIdOrderByCreatedAtDesc(playerId, playerId, pageable)
                .map(transaction -> new AuctionTransactionResponse(
                        transaction.getId(), transaction.getListing().getId(),
                        transaction.getBuyerPlayerId().equals(playerId) ? "BUY" : "SELL",
                        transaction.getItemDefinition().getCode(), transaction.getItemDefinition().getName(),
                        playerRepository.findById(transaction.getBuyerPlayerId()).map(player -> player.getUsername()).orElse("Jogador desconhecido"),
                        playerRepository.findById(transaction.getSellerPlayerId()).map(player -> player.getUsername()).orElse("Jogador desconhecido"),
                        transaction.getQuantity(), transaction.getUnitPrice(), transaction.getGrossAmount(),
                        transaction.getFee(), transaction.getSellerNetAmount(), transaction.getCreatedAt()))
                .getContent();
    }

    public GetAuctionHistoryUseCase(final AuctionTransactionRepository auctionTransactionRepository, final PlayerRepository playerRepository) {
        this.auctionTransactionRepository = auctionTransactionRepository;
        this.playerRepository = playerRepository;
    }
}
