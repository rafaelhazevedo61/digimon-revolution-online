package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.shop.api.dto.request.CreateShopProductRequest;
import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
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
        }
    }
}
