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
            throw new ConflictException("O produto da loja já existe: " + request.code());
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
            throw new ConflictException("O produto da loja já existe: " + request.code());
        }

        return AdminShopProductResponse.from(entity);
    }

    private void validateProductTypeFields(CreateShopProductRequest request) {
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

                String definitionCode = resolveItemDefinitionCode(
                        request.code(), request.itemType(), request.itemDefinitionCode());
                if (itemDefinitionRepository.findByCode(definitionCode)
                        .filter(definition -> "CHEST".equalsIgnoreCase(definition.getCategory()))
                        .isEmpty()) {
                    throw new NotFoundException("Definição do item de baú não encontrada: " + definitionCode);
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
