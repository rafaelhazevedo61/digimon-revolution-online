package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.shared.exception.NotFoundException;

import java.util.List;

public class ShopCatalog {

    private static final List<ShopProduct> PRODUCTS = List.of(

            new ShopProduct(
                    "TRAINING_STONE",
                    "Training Stone",
                    "A basic item used to improve Digimon growth.",
                    ShopProductType.ITEM,
                    ShopProductCategory.CONSUMABLE,
                    ItemType.TRAINING_STONE,
                    null,
                    100,
                    25
            ),

            new ShopProduct(
                    "DATA_CORE",
                    "Data Core",
                    "A material used in digital upgrades.",
                    ShopProductType.ITEM,
                    ShopProductCategory.MATERIAL,
                    ItemType.DATA_CORE,
                    null,
                    150,
                    40
            ),

            new ShopProduct(
                    "FRAGMENT_CHAMPION",
                    "Champion Fragment",
                    "Fragment required for Champion evolution.",
                    ShopProductType.ITEM,
                    ShopProductCategory.FRAGMENT,
                    ItemType.FRAGMENT_CHAMPION,
                    null,
                    300,
                    75
            ),

            new ShopProduct(
                    "FRAGMENT_ULTIMATE",
                    "Ultimate Fragment",
                    "Fragment required for Ultimate evolution.",
                    ShopProductType.ITEM,
                    ShopProductCategory.FRAGMENT,
                    ItemType.FRAGMENT_ULTIMATE,
                    null,
                    600,
                    150
            ),

            new ShopProduct(
                    "FRAGMENT_MEGA",
                    "Mega Fragment",
                    "Fragment required for Mega evolution.",
                    ShopProductType.ITEM,
                    ShopProductCategory.FRAGMENT,
                    ItemType.FRAGMENT_MEGA,
                    null,
                    1000,
                    250
            ),

            new ShopProduct(
                    "INCUBATOR_COMMON",
                    "Common Incubator",
                    "Common incubator used for Digitama incubation.",
                    ShopProductType.ITEM,
                    ShopProductCategory.CONSUMABLE,
                    ItemType.INCUBATOR_COMMON,
                    null,
                    500,
                    125
            ),

            new ShopProduct(
                    "IRON_CLAW",
                    "Iron Claw",
                    "A common weapon that increases attack.",
                    ShopProductType.EQUIPMENT,
                    ShopProductCategory.EQUIPMENT,
                    null,
                    "Iron Claw",
                    250,
                    60
            ),

            new ShopProduct(
                    "LEATHER_ARMOR",
                    "Leather Armor",
                    "A common armor that increases HP and defense.",
                    ShopProductType.EQUIPMENT,
                    ShopProductCategory.EQUIPMENT,
                    null,
                    "Leather Armor",
                    250,
                    60
            ),

            new ShopProduct(
                    "HOLY_RING",
                    "Holy Ring",
                    "A common accessory that increases basic stats.",
                    ShopProductType.EQUIPMENT,
                    ShopProductCategory.EQUIPMENT,
                    null,
                    "Holy Ring",
                    300,
                    75
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

    public static ShopProduct findByEquipmentTemplateName(String equipmentTemplateName) {
        return PRODUCTS.stream()
                .filter(ShopProduct::isEquipment)
                .filter(product -> product.getEquipmentTemplateName().equalsIgnoreCase(equipmentTemplateName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Shop equipment product not found: " + equipmentTemplateName));
    }
}