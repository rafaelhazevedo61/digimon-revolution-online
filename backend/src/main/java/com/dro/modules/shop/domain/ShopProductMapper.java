package com.dro.modules.shop.domain;

/**
 * Componente da camada de conversor entre domínio e contratos da API do módulo de Loja.
 */
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
