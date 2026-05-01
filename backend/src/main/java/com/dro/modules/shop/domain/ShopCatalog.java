package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.shared.exception.NotFoundException;

import java.util.List;

public class ShopCatalog {

    private static final List<ShopProduct> PRODUCTS = List.of(

            // Items
            new ShopProduct(
                    "POTION_SMALL",
                    "Small Potion",
                    "Restores a small amount of HP.",
                    ShopProductType.ITEM,
                    ItemType.POTION_SMALL,
                    null,
                    50,
                    true,
                    10
            ),

            new ShopProduct(
                    "DATA_CORE",
                    "Data Core",
                    "A core that can be used to rebirth your digimon",
                    ShopProductType.ITEM,
                    ItemType.DATA_CORE,
                    null,
                    80,
                    true,
                    20
            ),

            // Equipments
            new ShopProduct(
                    "IRON_CLAW",
                    "Iron Claw",
                    "A common weapon that increases attack.",
                    ShopProductType.EQUIPMENT,
                    null,
                    "Iron Claw",
                    150,
                    true,
                    30
            ),

            new ShopProduct(
                    "LEATHER_ARMOR",
                    "Leather Armor",
                    "A common armor that increases HP and defense.",
                    ShopProductType.EQUIPMENT,
                    null,
                    "Leather Armor",
                    150,
                    true,
                    30
            ),

            new ShopProduct(
                    "HOLY_RING",
                    "Holy Ring",
                    "A common accessory that increases all basic stats.",
                    ShopProductType.EQUIPMENT,
                    null,
                    "Holy Ring",
                    200,
                    true,
                    40
            )
    );

    public static List<ShopProduct> getProducts() {
        return PRODUCTS;
    }

    public static ShopProduct findByCode(String code) {
        return PRODUCTS.stream()
                .filter(product -> product.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Shop product not found: " + code));
    }
}