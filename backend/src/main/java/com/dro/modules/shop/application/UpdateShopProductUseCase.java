package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
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
        validateProductTypeFields(code, request);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setCategory(request.category());
        entity.setItemType(request.itemType());
        entity.setItemDefinitionCode(resolveItemDefinitionCode(code, request.itemType(), request.itemDefinitionCode()));
        entity.setEquipmentTemplateName(request.equipmentTemplateName());
        entity.setPrice(request.price());
        entity.setSellPrice(request.sellPrice());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");
        shopProductRepository.save(entity);
        return AdminShopProductResponse.from(entity);
    }

    private void validateProductTypeFields(String code, UpdateShopProductRequest request) {
        if (request.productType() == ShopProductType.EQUIPMENT) {
            if (request.equipmentTemplateName() == null || request.equipmentTemplateName().isBlank()) {
                throw new BadRequestException("O modelo de equipamento é obrigatório para produtos do tipo Equipamento");
            }
            if (equipmentTemplateRepository.findByName(request.equipmentTemplateName()).isEmpty()) {
                throw new NotFoundException("Modelo de equipamento não encontrado: " + request.equipmentTemplateName());
            }
        }
        if (request.productType() == ShopProductType.ITEM) {
            if (request.itemType() == null) {
                throw new BadRequestException("O tipo do item é obrigatório para produtos do tipo Item");
            }
            if (request.category() == ShopProductCategory.CHEST) {
                if (request.itemType() != ItemType.LOOT_CHEST) {
                    throw new BadRequestException("Produtos da categoria Baú devem usar o tipo de item LOOT_CHEST");
                }
                String definitionCode = resolveItemDefinitionCode(code, request.itemType(), request.itemDefinitionCode());
                if (definitionCode == null || definitionCode.isBlank()) {
                    throw new BadRequestException("O código da definição é obrigatório para produtos da categoria Baú");
                }
                if (itemDefinitionRepository.findByCode(definitionCode).filter(definition -> "CHEST".equalsIgnoreCase(definition.getCategory())).isEmpty()) {
                    throw new NotFoundException("Definição do item de baú não encontrada: " + definitionCode);
                }
            }
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
