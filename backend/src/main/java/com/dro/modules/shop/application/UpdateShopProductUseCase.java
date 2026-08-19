package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.shop.api.dto.request.UpdateShopProductRequest;
import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
@RequiredArgsConstructor
public class UpdateShopProductUseCase {

    private final ShopProductRepository shopProductRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;

    @CacheEvict(cacheNames = "shopCatalog", key = "'active'")
    @Transactional
    public AdminShopProductResponse execute(String code, UpdateShopProductRequest request) {

        ShopProductEntity entity = shopProductRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Shop product not found: " + code));

        validateProductTypeFields(request);

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setCategory(request.category());
        entity.setItemType(request.itemType());
        entity.setEquipmentTemplateName(request.equipmentTemplateName());
        entity.setPrice(request.price());
        entity.setSellPrice(request.sellPrice());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        shopProductRepository.save(entity);

        return AdminShopProductResponse.from(entity);
    }

    private void validateProductTypeFields(UpdateShopProductRequest request) {
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
        }
    }
}
