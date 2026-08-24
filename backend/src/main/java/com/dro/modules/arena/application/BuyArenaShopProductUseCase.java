package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.request.BuyArenaShopRequest;
import com.dro.modules.arena.api.dto.response.BuyArenaShopResponse;
import com.dro.modules.arena.domain.ArenaShopProduct;
import com.dro.modules.arena.infra.ArenaShopProductRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Arena.
 */
@Service
public class BuyArenaShopProductUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ArenaShopProductRepository arenaShopProductRepository;
    private final AddItemUseCase addItemUseCase;

    @Transactional
    public BuyArenaShopResponse execute(String token, BuyArenaShopRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Active digimon does not belong to this player");
        }
        ArenaShopProduct product = arenaShopProductRepository.findById(request.productCode()).filter(ArenaShopProduct::isActive).orElseThrow(() -> new NotFoundException("Arena shop product not found: " + request.productCode()));
        int totalPrice = product.getPriceCoins() * request.quantity();
        if (player.getArenaCoins() < totalPrice) {
            throw new UnprocessableException("Not enough arena coins");
        }
        addItemUseCase.execute(digimon.getId(), product.getItemType(), product.getQuantity() * request.quantity());
        player.setArenaCoins(player.getArenaCoins() - totalPrice);
        playerRepository.save(player);
        return new BuyArenaShopResponse(product.getCode(), product.getName(), product.getItemType(), product.getQuantity() * request.quantity(), totalPrice, player.getArenaCoins(), "Purchase successful");
    }

    public BuyArenaShopProductUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final ArenaShopProductRepository arenaShopProductRepository, final AddItemUseCase addItemUseCase) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.arenaShopProductRepository = arenaShopProductRepository;
        this.addItemUseCase = addItemUseCase;
    }
}
