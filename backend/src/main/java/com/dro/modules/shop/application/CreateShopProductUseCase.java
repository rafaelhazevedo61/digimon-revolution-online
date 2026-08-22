package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.shop.api.dto.request.CreateShopProductRequest;
import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
@RequiredArgsConstructor
public class CreateShopProductUseCase {

    private final ShopProductRepository shopProductRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @CacheEvict(cacheNames = "shopCatalog", key = "'active'")
    @Transactional
    public AdminShopProductResponse execute(CreateShopProductRequest request) {

        if (shopProductRepository.existsById(request.code())) {
            throw new ConflictException("Shop product already exists: " + request.code());
        }

        validateProductTypeFields(request);

        LocalDateTime now = LocalDateTime.now();

        ShopProductEntity entity = ShopProductEntity.builder()
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .productType(request.productType())
                .category(request.category())
                .itemType(request.itemType())
                .itemDefinitionCode(resolveItemDefinitionCode(request.code(), request.itemType(), request.itemDefinitionCode()))
                .equipmentTemplateName(request.equipmentTemplateName())
                .price(request.price())
                .sellPrice(request.sellPrice())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("admin")
                .updatedBy("admin")
                .newEntity(true)
                .build();

        try {
            shopProductRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Shop product already exists: " + request.code());
        }

        return AdminShopProductResponse.from(entity);
    }

    private void validateProductTypeFields(CreateShopProductRequest request) {
        if (request.productType() == ShopProductType.EQUIPMENT) {
            if (request.equipmentTemplateName() == null || request.equipmentTemplateName().isBlank()) {
                throw new BadRequestException("equipmentTemplateName is required for EQUIPMENT products");
            }
            if (equipmentTemplateRepository.findByName(request.equipmentTemplateName()).isEmpty()) {
                throw new NotFoundException("Equipment template not found: " + request.equipmentTemplateName());
            }
        }

        if (request.productType() == ShopProductType.ITEM) {
            if (request.itemType() == null) {
                throw new BadRequestException("itemType is required for ITEM products");
            }

            if (request.category() == ShopProductCategory.CHEST) {
                if (request.itemType() != ItemType.LOOT_CHEST) {
                    throw new BadRequestException("CHEST products must use itemType LOOT_CHEST");
                }

                String definitionCode = resolveItemDefinitionCode(
                        request.code(), request.itemType(), request.itemDefinitionCode());
                if (itemDefinitionRepository.findByCode(definitionCode)
                        .filter(definition -> "CHEST".equalsIgnoreCase(definition.getCategory()))
                        .isEmpty()) {
                    throw new NotFoundException("Chest item definition not found: " + definitionCode);
                }
            }
        }
    }

    private String resolveItemDefinitionCode(String productCode, ItemType itemType, String requestedCode) {
        if (requestedCode != null && !requestedCode.isBlank()) {
            return requestedCode.trim();
        }
        if (itemType == ItemType.LOOT_CHEST) {
            return productCode;
        }
        if (itemType != null && itemType != ItemType.EVOLUTION_MATERIAL) {
            return itemType.name();
        }
        return null;
    }
}
