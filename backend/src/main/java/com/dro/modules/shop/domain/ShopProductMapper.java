package com.dro.modules.shop.domain;

public class ShopProductMapper {

    private ShopProductMapper() {}

    public static ShopProduct toProduct(ShopProductEntity entity) {
        return new ShopProduct(
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductType(),
                entity.getCategory(),
                entity.getItemType(),
                entity.getEquipmentTemplateName(),
                entity.getPrice(),
                entity.getSellPrice()
        );
    }
}
