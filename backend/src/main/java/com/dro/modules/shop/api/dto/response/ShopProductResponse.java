package com.dro.modules.shop.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.ShopProduct;
import com.dro.modules.shop.domain.ShopProductType;

public record ShopProductResponse(
        String code,
        String name,
        String description,
        ShopProductType productType,
        ItemType itemType,
        String equipmentTemplateName,
        int price,
        boolean sellable,
        int sellPrice
) {
    public static ShopProductResponse from(ShopProduct product) {
        return new ShopProductResponse(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getProductType(),
                product.getItemType(),
                product.getEquipmentTemplateName(),
                product.getPrice(),
                product.isSellable(),
                product.getSellPrice()
        );
    }
}