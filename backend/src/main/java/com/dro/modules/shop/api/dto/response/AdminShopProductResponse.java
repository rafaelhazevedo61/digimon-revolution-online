package com.dro.modules.shop.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;

import java.time.LocalDateTime;

public record AdminShopProductResponse(
        String code,
        String name,
        String description,
        ShopProductType productType,
        ShopProductCategory category,
        ItemType itemType,
        String equipmentTemplateName,
        int price,
        int sellPrice,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
    public static AdminShopProductResponse from(ShopProductEntity entity) {
        return new AdminShopProductResponse(
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductType(),
                entity.getCategory(),
                entity.getItemType(),
                entity.getEquipmentTemplateName(),
                entity.getPrice(),
                entity.getSellPrice(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
}
