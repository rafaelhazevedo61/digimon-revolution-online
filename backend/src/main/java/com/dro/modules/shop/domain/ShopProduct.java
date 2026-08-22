package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;

/**
 * Componente da camada de componente de domínio do módulo de Loja.
 */
public class ShopProduct {

    private final String code;
    private final String name;
    private final String description;
    private final ShopProductType productType;
    private final ShopProductCategory category;
    private final ItemType itemType;
    private final String itemDefinitionCode;
    private final String equipmentTemplateName;
    private final int price;
    private final int sellPrice;

    public ShopProduct(
            String code,
            String name,
            String description,
            ShopProductType productType,
            ShopProductCategory category,
            ItemType itemType,
            String itemDefinitionCode,
            String equipmentTemplateName,
            int price,
            int sellPrice
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.productType = productType;
        this.category = category;
        this.itemType = itemType;
        this.itemDefinitionCode = itemDefinitionCode;
        this.equipmentTemplateName = equipmentTemplateName;
        this.price = price;
        this.sellPrice = sellPrice;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ShopProductType getProductType() {
        return productType;
    }

    public ShopProductCategory getCategory() {
        return category;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getItemDefinitionCode() {
        return itemDefinitionCode;
    }

    public String getEquipmentTemplateName() {
        return equipmentTemplateName;
    }

    public int getPrice() {
        return price;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public boolean isItem() {
        return productType == ShopProductType.ITEM;
    }

    public boolean isEquipment() {
        return productType == ShopProductType.EQUIPMENT;
    }
}