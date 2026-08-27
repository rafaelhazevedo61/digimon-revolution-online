package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.shop.api.dto.request.UpdateShopProductRequest;
import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
public class UpdateShopProductUseCase {
    private final ShopProductRepository shopProductRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @CacheEvict(cacheNames = "shopCatalog", key = "\'active\'")
    @Transactional
    public AdminShopProductResponse execute(String code, UpdateShopProductRequest request) {
        ShopProductEntity entity = shopProductRepository.findById(code).orElseThrow(() -> new NotFoundException("Produto da loja não encontrado: " + code));
        String itemDefinitionCode = resolveItemDefinitionCode(code, request.itemType(), request.itemDefinitionCode());
        ItemType resolvedItemType = resolveItemType(request.itemType(), itemDefinitionCode);
        validateProductTypeFields(code, request, itemDefinitionCode, resolvedItemType);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setCategory(request.category());
        entity.setItemType(resolvedItemType);
        entity.setItemDefinitionCode(itemDefinitionCode);
        entity.setEquipmentTemplateName(request.equipmentTemplateName());
        entity.setPrice(request.price());
        entity.setSellPrice(request.sellPrice());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");
        shopProductRepository.save(entity);
        return AdminShopProductResponse.from(entity);
    }

    private void validateProductTypeFields(String code, UpdateShopProductRequest request, String definitionCode, ItemType resolvedItemType) {
        if (request.productType() == ShopProductType.EQUIPMENT) {
            if (request.equipmentTemplateName() == null || request.equipmentTemplateName().isBlank()) {
                throw new BadRequestException("O modelo de equipamento é obrigatório para produtos do tipo Equipamento");
            }
            if (equipmentTemplateRepository.findByName(request.equipmentTemplateName()).isEmpty()) {
                throw new NotFoundException("Modelo de equipamento não encontrado: " + request.equipmentTemplateName());
            }
        }
        if (request.productType() == ShopProductType.ITEM) {
            if (resolvedItemType == null) {
                throw new BadRequestException("Informe o tipo do item ou o código da definição do item");
            }
            if (definitionCode != null && itemDefinitionRepository.findByCode(definitionCode).isEmpty()) {
                throw new NotFoundException("Definição do item não encontrada: " + definitionCode);
            }
            if (request.category() == ShopProductCategory.CHEST) {
                if (resolvedItemType != ItemType.LOOT_CHEST) {
                    throw new BadRequestException("Produtos da categoria Baú devem usar o tipo de item LOOT_CHEST");
                }
                if (definitionCode == null || itemDefinitionRepository.findByCode(definitionCode).filter(definition -> "CHEST".equalsIgnoreCase(definition.getCategory())).isEmpty()) {
                    throw new NotFoundException("Definição do item de baú não encontrada: " + definitionCode);
                }
            }
        }
    }

    private ItemType resolveItemType(ItemType requestedType, String definitionCode) {
        if (requestedType != null) {
            return requestedType;
        }
        if (definitionCode == null || definitionCode.isBlank()) {
            return null;
        }
        ItemDefinition definition = itemDefinitionRepository.findByCode(definitionCode)
                .orElseThrow(() -> new NotFoundException("Definição do item não encontrada: " + definitionCode));
        if ("CHEST".equalsIgnoreCase(definition.getCategory())) {
            return ItemType.LOOT_CHEST;
        }
        try {
            return ItemType.valueOf(definition.getCode().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }

    private String resolveItemDefinitionCode(String productCode, ItemType itemType, String requestedCode) {
        if (requestedCode != null && !requestedCode.isBlank()) {
            return requestedCode.trim();
        }
        if (itemType == ItemType.LOOT_CHEST && productCode != null && !productCode.isBlank()) {
            return productCode;
        }
        if (itemType != null && itemType != ItemType.EVOLUTION_MATERIAL) {
            return itemType.name();
        }
        return null;
    }

    public UpdateShopProductUseCase(final ShopProductRepository shopProductRepository, final EquipmentTemplateRepository equipmentTemplateRepository, final ItemDefinitionRepository itemDefinitionRepository) {
        this.shopProductRepository = shopProductRepository;
        this.equipmentTemplateRepository = equipmentTemplateRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
