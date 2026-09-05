package com.dro.modules.arena.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.ShopProductType;
import jakarta.persistence.*;

/**
 * Componente da camada de componente de domínio do módulo de Arena.
 */
@Entity
@Table(name = "arena_shop_products")
public class ArenaShopProduct {
    @Id
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ShopProductType productType;
    @Column(name = "equipment_template_name")
    private String equipmentTemplateName;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "price_coins", nullable = false)
    private int priceCoins;
    @Column(name = "active", nullable = false)
    private boolean active;


    public static class ArenaShopProductBuilder {
        private String code;
        private String name;
        private ShopProductType productType = ShopProductType.ITEM;
        private String equipmentTemplateName;
        private ItemType itemType;
        private int quantity;
        private int priceCoins;
        private boolean active;

        ArenaShopProductBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder name(final String name) {
            this.name = name;
            return this;
        }
        public ArenaShopProduct.ArenaShopProductBuilder productType(final ShopProductType productType) {
            this.productType = productType;
            return this;
        }
        public ArenaShopProduct.ArenaShopProductBuilder equipmentTemplateName(final String equipmentTemplateName) {
            this.equipmentTemplateName = equipmentTemplateName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder priceCoins(final int priceCoins) {
            this.priceCoins = priceCoins;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ArenaShopProduct.ArenaShopProductBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        public ArenaShopProduct build() {
            return new ArenaShopProduct(this.code, this.name, this.productType, this.itemType, this.equipmentTemplateName, this.quantity, this.priceCoins, this.active);
        }

        @Override
        public String toString() {
            return "ArenaShopProduct.ArenaShopProductBuilder(code=" + this.code + ", name=" + this.name + ", itemType=" + this.itemType + ", quantity=" + this.quantity + ", priceCoins=" + this.priceCoins + ", active=" + this.active + ")";
        }
    }

    public static ArenaShopProduct.ArenaShopProductBuilder builder() {
        return new ArenaShopProduct.ArenaShopProductBuilder();
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }
    public ShopProductType getProductType() {
        return this.productType;
    }
    public String getEquipmentTemplateName() {
        return this.equipmentTemplateName;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getPriceCoins() {
        return this.priceCoins;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }
    public void setProductType(final ShopProductType productType) {
        this.productType = productType;
    }
    public void setEquipmentTemplateName(final String equipmentTemplateName) {
        this.equipmentTemplateName = equipmentTemplateName;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setPriceCoins(final int priceCoins) {
        this.priceCoins = priceCoins;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public ArenaShopProduct() {
    }

    public ArenaShopProduct(final String code, final String name, final ShopProductType productType, final ItemType itemType, final String equipmentTemplateName, final int quantity, final int priceCoins, final boolean active) {
        this.code = code;
        this.name = name;
        this.productType = productType;
        this.itemType = itemType;
        this.equipmentTemplateName = equipmentTemplateName;
        this.quantity = quantity;
        this.priceCoins = priceCoins;
        this.active = active;
    }
}
