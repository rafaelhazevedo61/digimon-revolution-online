package com.dro.modules.shop.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.ShopProduct;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;

/**
 * Contrato de dados do módulo de Loja.
 */
public record ShopProductResponse(
        String code,
        String name,
        String description,
        ShopProductType productType,
        ShopProductCategory category,
        ItemType itemType,
        String equipmentTemplateName,
        int price,
        int sellPrice
) {
    public static ShopProductResponse from(ShopProduct product) {
        return new ShopProductResponse(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getProductType(),
                product.getCategory(),
                product.getItemType(),
                product.getEquipmentTemplateName(),
                product.getPrice(),
                product.getSellPrice()
        );
    }
}