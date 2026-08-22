package com.dro.modules.shop.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.application.ConsumeItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.shop.api.dto.request.SellShopProductRequest;
import com.dro.modules.shop.api.dto.response.SellShopProductResponse;
import com.dro.modules.shop.domain.ShopProduct;
import com.dro.modules.shop.domain.ShopProductMapper;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
@RequiredArgsConstructor
public class SellShopProductUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ConsumeItemUseCase consumeItemUseCase;
    private final EquipmentRepository equipmentRepository;
    private final ShopProductRepository shopProductRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public SellShopProductResponse execute(String token, SellShopProductRequest request) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("Nenhum Digimon ativo selecionado");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon ativo não encontrado"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("O Digimon ativo não pertence a este jogador");
        }

        if (request.equipmentId() != null) {
            return sellEquipment(digimon, request);
        }

        if (request.productCode() != null && !request.productCode().isBlank()) {
            return sellItem(digimon, request);
        }

        if (request.itemType() != null && !request.itemType().isBlank()) {
            return sellByItemDefinition(digimon, request);
        }

        throw new BadRequestException("Informe o produto, o equipamento ou o item que deseja vender");
    }

    private SellShopProductResponse sellItem(Digimon digimon, SellShopProductRequest request) {

        if (request.quantity() <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }

        ShopProduct product = shopProductRepository.findById(request.productCode())
                .map(ShopProductMapper::toProduct)
                .orElseThrow(() -> new NotFoundException("Produto da loja não encontrado: " + request.productCode()));

        if (!product.isItem()) {
            throw new BadRequestException("O produto não é um item de inventário");
        }

        if (product.getSellPrice() <= 0) {
            throw new UnprocessableException("Este produto não pode ser vendido");
        }

        int totalSellPrice = product.getSellPrice() * request.quantity();

        consumeItemUseCase.execute(
                digimon.getId(),
                product.getItemType(),
                request.quantity()
        );

        digimon.setBits(digimon.getBits() + totalSellPrice);
        digimonRepository.save(digimon);

        return new SellShopProductResponse(
                product.getCode(),
                product.getName(),
                product.getProductType(),
                request.quantity(),
                totalSellPrice,
                digimon.getBits(),
                "Produto vendido com sucesso"
        );
    }

    private SellShopProductResponse sellEquipment(Digimon digimon, SellShopProductRequest request) {

        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("Equipamento não encontrado"));

        if (!equipment.getDigimonId().equals(digimon.getId())) {
            throw new ForbiddenException("O equipamento não pertence ao Digimon ativo");
        }

        if (equipment.isEquipped()) {
            throw new ConflictException("Equipamentos equipados não podem ser vendidos");
        }

        ShopProduct product = shopProductRepository.findByEquipmentTemplateNameIgnoreCase(equipment.getName())
                .map(ShopProductMapper::toProduct)
                .orElseThrow(() -> new NotFoundException("Produto de equipamento da loja não encontrado: " + equipment.getName()));

        if (product.getSellPrice() <= 0) {
            throw new UnprocessableException("Este equipamento não pode ser vendido");
        }

        equipmentRepository.delete(equipment);

        digimon.setBits(digimon.getBits() + product.getSellPrice());
        digimonRepository.save(digimon);

        return new SellShopProductResponse(
                product.getCode(),
                product.getName(),
                product.getProductType(),
                1,
                product.getSellPrice(),
                digimon.getBits(),
                "Equipamento vendido com sucesso"
        );
    }

    private SellShopProductResponse sellByItemDefinition(Digimon digimon, SellShopProductRequest request) {

        if (request.quantity() <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }

        ItemDefinition itemDef = itemDefinitionRepository.findByCode(request.itemType())
                .orElseThrow(() -> new NotFoundException("Definição do item não encontrada: " + request.itemType()));

        if (!itemDef.isSellable()) {
            throw new UnprocessableException("Este item não pode ser vendido");
        }

        if (itemDef.getSellPrice() == null || itemDef.getSellPrice() <= 0) {
            throw new UnprocessableException("Este item não possui preço de venda");
        }

        InventoryItem inventoryItem = inventoryRepository.findByDigimonIdAndItemDefinitionId(
                digimon.getId(), itemDef.getId()
        ).orElseThrow(() -> new NotFoundException("Item não encontrado no inventário"));

        if (inventoryItem.getQuantity() < request.quantity()) {
            throw new BadRequestException("Quantidade insuficiente. Você possui " + inventoryItem.getQuantity());
        }

        int totalSellPrice = itemDef.getSellPrice() * request.quantity();

        consumeItemUseCase.consumeMaterial(
                digimon.getId(),
                itemDef.getId(),
                request.quantity()
        );

        digimon.setBits(digimon.getBits() + totalSellPrice);
        digimonRepository.save(digimon);

        return new SellShopProductResponse(
                itemDef.getCode(),
                itemDef.getName(),
                ShopProductType.ITEM,
                request.quantity(),
                totalSellPrice,
                digimon.getBits(),
                "Item vendido com sucesso"
        );
    }
}