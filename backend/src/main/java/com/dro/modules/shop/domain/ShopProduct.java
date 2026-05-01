package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;

public class ShopProduct {

    private final String code;
    private final String name;
    private final String description;
    private final ShopProductType productType;
    private final ItemType itemType;
    private final String equipmentTemplateName;
    private final int price;
    private final boolean sellable;
    private final int sellPrice;

    public ShopProduct(
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
        this.code = code;
        this.name = name;
        this.description = description;
        this.productType = productType;
        this.itemType = itemType;
        this.equipmentTemplateName = equipmentTemplateName;
        this.price = price;
        this.sellable = sellable;
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

    public ItemType getItemType() {
        return itemType;
    }

    public String getEquipmentTemplateName() {
        return equipmentTemplateName;
    }

    public int getPrice() {
        return price;
    }

    public boolean isSellable() {
        return sellable;
    }

    public int getSellPrice() {
        return sellPrice;
    }
}