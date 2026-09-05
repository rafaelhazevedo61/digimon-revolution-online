package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.request.BuyArenaShopRequest;
import com.dro.modules.arena.api.dto.response.BuyArenaShopResponse;
import com.dro.modules.arena.domain.ArenaShopProduct;
import com.dro.modules.arena.infra.ArenaShopProductRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.equipment.domain.EquipmentRarityRules;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
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
    private static final int MAX_STACK_QUANTITY = 999;

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ArenaShopProductRepository arenaShopProductRepository;
    private final InventoryRepository inventoryRepository;
    private final AddItemUseCase addItemUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;

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
        if (product.getProductType() == ShopProductType.EQUIPMENT && request.quantity() != 1) {
            throw new BadRequestException("Equipamentos devem ser comprados um por vez");
        }
        if (product.getProductType() == ShopProductType.ITEM) {
            validateItemStack(playerId, product, request.quantity());
        }
        int totalPrice = product.getPriceCoins() * request.quantity();
        if (player.getArenaCoins() < totalPrice) {
            throw new UnprocessableException("Not enough arena coins");
        }
        if (product.getProductType() == ShopProductType.EQUIPMENT) {
            grantEquipmentUseCase.execute(digimon.getId(), product.getEquipmentTemplateName(), EquipmentRarityRules.rollRarity("ARENA_SHOP"));
        } else {
            addItemUseCase.execute(digimon.getId(), product.getItemType(), product.getQuantity() * request.quantity());
        }
        player.setArenaCoins(player.getArenaCoins() - totalPrice);
        playerRepository.save(player);
        return new BuyArenaShopResponse(product.getCode(), product.getName(), product.getItemType(), product.getQuantity() * request.quantity(), totalPrice, player.getArenaCoins(), "Purchase successful");
    }

    private void validateItemStack(UUID playerId, ArenaShopProduct product, int purchaseQuantity) {
        InventoryItem inventoryItem = inventoryRepository
                .findByPlayerIdAndItemTypeForUpdate(playerId, product.getItemType())
                .orElse(null);
        int currentQuantity = inventoryItem == null ? 0 : Math.max(0, inventoryItem.getQuantity());
        int remainingStackQuantity = Math.max(0, MAX_STACK_QUANTITY - currentQuantity);
        long requestedItemQuantity = (long) product.getQuantity() * purchaseQuantity;
        if (requestedItemQuantity > remainingStackQuantity) {
            throw new BadRequestException("A quantidade ultrapassa o limite de stack. Espaço restante: " + remainingStackQuantity);
        }
    }

    public BuyArenaShopProductUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final ArenaShopProductRepository arenaShopProductRepository, final InventoryRepository inventoryRepository, final AddItemUseCase addItemUseCase, final GrantEquipmentUseCase grantEquipmentUseCase) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.arenaShopProductRepository = arenaShopProductRepository;
        this.inventoryRepository = inventoryRepository;
        this.addItemUseCase = addItemUseCase;
        this.grantEquipmentUseCase = grantEquipmentUseCase;
    }
}
