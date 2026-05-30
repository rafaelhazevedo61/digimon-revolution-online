package com.dro.modules.shop.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.shop.api.dto.BuyShopProductResponse;
import com.dro.modules.shop.api.dto.request.BuyShopProductRequest;
import com.dro.modules.shop.domain.ShopProduct;
import com.dro.modules.shop.domain.ShopProductMapper;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuyShopProductUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;
    private final ShopProductRepository shopProductRepository;

    @Transactional
    public BuyShopProductResponse execute(String token, BuyShopProductRequest request) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Active digimon does not belong to this player");
        }

        ShopProduct product = shopProductRepository.findById(request.productCode())
                .map(ShopProductMapper::toProduct)
                .orElseThrow(() -> new NotFoundException("Shop product not found: " + request.productCode()));

        int quantity = request.quantity();

        if (product.getProductType() == ShopProductType.EQUIPMENT && quantity > 1) {
            throw new BadRequestException("Equipment must be purchased one at a time");
        }

        int totalPrice = product.getPrice() * quantity;

        if (digimon.getBits() < totalPrice) {
            throw new UnprocessableException("Not enough bits");
        }

        UUID equipmentId = null;

        if (product.getProductType() == ShopProductType.ITEM) {
            addItemUseCase.execute(
                    digimon.getId(),
                    product.getItemType(),
                    quantity
            );
        }

        if (product.getProductType() == ShopProductType.EQUIPMENT) {
            equipmentId = grantEquipmentUseCase.execute(
                    digimon.getId(),
                    product.getEquipmentTemplateName()
            );
        }

        digimon.setBits(digimon.getBits() - totalPrice);
        digimonRepository.save(digimon);

        return new BuyShopProductResponse(
                product.getCode(),
                product.getName(),
                product.getProductType(),
                quantity,
                totalPrice,
                digimon.getBits(),
                equipmentId,
                "Product purchased successfully"
        );
    }
}