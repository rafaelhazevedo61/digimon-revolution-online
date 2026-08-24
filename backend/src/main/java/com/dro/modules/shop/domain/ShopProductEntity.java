package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;

/**
 * Componente da camada de componente de domínio do módulo de Loja.
 */
@Entity
@Table(name = "shop_products")
public class ShopProductEntity implements Persistable<String> {
    @Id
    private String code;
    @Column(nullable = false)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ShopProductType productType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopProductCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType;
    @Column(name = "item_definition_code", length = 80)
    private String itemDefinitionCode;
    @Column(name = "equipment_template_name")
    private String equipmentTemplateName;
    @Column(nullable = false)
    private int price;
    @Column(name = "sell_price", nullable = false)
    private int sellPrice;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
    @Transient
    private boolean newEntity;

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    private static int $default$sellPrice() {
        return 0;
    }

    private static boolean $default$active() {
        return true;
    }

    private static boolean $default$newEntity() {
        return false;
    }


    public static class ShopProductEntityBuilder {
        private String code;
        private String name;
        private String description;
        private ShopProductType productType;
        private ShopProductCategory category;
        private ItemType itemType;
        private String itemDefinitionCode;
        private String equipmentTemplateName;
        private int price;
        private boolean sellPrice$set;
        private int sellPrice$value;
        private boolean active$set;
        private boolean active$value;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private boolean newEntity$set;
        private boolean newEntity$value;

        ShopProductEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder productType(final ShopProductType productType) {
            this.productType = productType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder category(final ShopProductCategory category) {
            this.category = category;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder itemDefinitionCode(final String itemDefinitionCode) {
            this.itemDefinitionCode = itemDefinitionCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder equipmentTemplateName(final String equipmentTemplateName) {
            this.equipmentTemplateName = equipmentTemplateName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder price(final int price) {
            this.price = price;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder sellPrice(final int sellPrice) {
            this.sellPrice$value = sellPrice;
            sellPrice$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder updatedBy(final String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ShopProductEntity.ShopProductEntityBuilder newEntity(final boolean newEntity) {
            this.newEntity$value = newEntity;
            newEntity$set = true;
            return this;
        }

        public ShopProductEntity build() {
            int sellPrice$value = this.sellPrice$value;
            if (!this.sellPrice$set) sellPrice$value = ShopProductEntity.$default$sellPrice();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = ShopProductEntity.$default$active();
            boolean newEntity$value = this.newEntity$value;
            if (!this.newEntity$set) newEntity$value = ShopProductEntity.$default$newEntity();
            return new ShopProductEntity(this.code, this.name, this.description, this.productType, this.category, this.itemType, this.itemDefinitionCode, this.equipmentTemplateName, this.price, sellPrice$value, active$value, this.createdAt, this.updatedAt, this.createdBy, this.updatedBy, newEntity$value);
        }

        @Override
        public String toString() {
            return "ShopProductEntity.ShopProductEntityBuilder(code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", productType=" + this.productType + ", category=" + this.category + ", itemType=" + this.itemType + ", itemDefinitionCode=" + this.itemDefinitionCode + ", equipmentTemplateName=" + this.equipmentTemplateName + ", price=" + this.price + ", sellPrice$value=" + this.sellPrice$value + ", active$value=" + this.active$value + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdBy=" + this.createdBy + ", updatedBy=" + this.updatedBy + ", newEntity$value=" + this.newEntity$value + ")";
        }
    }

    public static ShopProductEntity.ShopProductEntityBuilder builder() {
        return new ShopProductEntity.ShopProductEntityBuilder();
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public ShopProductType getProductType() {
        return this.productType;
    }

    public ShopProductCategory getCategory() {
        return this.category;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public String getItemDefinitionCode() {
        return this.itemDefinitionCode;
    }

    public String getEquipmentTemplateName() {
        return this.equipmentTemplateName;
    }

    public int getPrice() {
        return this.price;
    }

    public int getSellPrice() {
        return this.sellPrice;
    }

    public boolean isActive() {
        return this.active;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public boolean isNewEntity() {
        return this.newEntity;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setProductType(final ShopProductType productType) {
        this.productType = productType;
    }

    public void setCategory(final ShopProductCategory category) {
        this.category = category;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setItemDefinitionCode(final String itemDefinitionCode) {
        this.itemDefinitionCode = itemDefinitionCode;
    }

    public void setEquipmentTemplateName(final String equipmentTemplateName) {
        this.equipmentTemplateName = equipmentTemplateName;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public void setSellPrice(final int sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setNewEntity(final boolean newEntity) {
        this.newEntity = newEntity;
    }

    public ShopProductEntity() {
        this.sellPrice = ShopProductEntity.$default$sellPrice();
        this.active = ShopProductEntity.$default$active();
        this.newEntity = ShopProductEntity.$default$newEntity();
    }

    public ShopProductEntity(final String code, final String name, final String description, final ShopProductType productType, final ShopProductCategory category, final ItemType itemType, final String itemDefinitionCode, final String equipmentTemplateName, final int price, final int sellPrice, final boolean active, final LocalDateTime createdAt, final LocalDateTime updatedAt, final String createdBy, final String updatedBy, final boolean newEntity) {
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
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.newEntity = newEntity;
    }
}
