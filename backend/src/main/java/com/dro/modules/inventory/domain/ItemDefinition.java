package com.dro.modules.inventory.domain;

import jakarta.persistence.*;

/**
 * Componente da camada de modelo de domínio do módulo de Inventário.
 */
@Entity
@Table(name = "item_definitions")
public class ItemDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false)
    private boolean stackable;
    @Column(name = "buy_price")
    private Integer buyPrice;
    @Column(name = "sell_price")
    private Integer sellPrice;
    @Column(nullable = false)
    private boolean tradable;
    @Column(nullable = false)
    private boolean sellable;
    @Column(nullable = false)
    private boolean usable;
    @Column(name = "max_stack")
    private Integer maxStack;
    @Column(nullable = false, length = 20)
    private String rarity;
    @Column(length = 120)
    private String icon;


    public static class ItemDefinitionBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private String category;
        private boolean stackable;
        private Integer buyPrice;
        private Integer sellPrice;
        private boolean tradable;
        private boolean sellable;
        private boolean usable;
        private Integer maxStack;
        private String rarity;
        private String icon;

        ItemDefinitionBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder category(final String category) {
            this.category = category;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder stackable(final boolean stackable) {
            this.stackable = stackable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder buyPrice(final Integer buyPrice) {
            this.buyPrice = buyPrice;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder sellPrice(final Integer sellPrice) {
            this.sellPrice = sellPrice;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder tradable(final boolean tradable) {
            this.tradable = tradable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder sellable(final boolean sellable) {
            this.sellable = sellable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder usable(final boolean usable) {
            this.usable = usable;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder maxStack(final Integer maxStack) {
            this.maxStack = maxStack;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder rarity(final String rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ItemDefinition.ItemDefinitionBuilder icon(final String icon) {
            this.icon = icon;
            return this;
        }

        public ItemDefinition build() {
            return new ItemDefinition(this.id, this.code, this.name, this.description, this.category, this.stackable, this.buyPrice, this.sellPrice, this.tradable, this.sellable, this.usable, this.maxStack, this.rarity, this.icon);
        }

        @Override
        public String toString() {
            return "ItemDefinition.ItemDefinitionBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", category=" + this.category + ", stackable=" + this.stackable + ", buyPrice=" + this.buyPrice + ", sellPrice=" + this.sellPrice + ", tradable=" + this.tradable + ", sellable=" + this.sellable + ", usable=" + this.usable + ", maxStack=" + this.maxStack + ", rarity=" + this.rarity + ", icon=" + this.icon + ")";
        }
    }

    public static ItemDefinition.ItemDefinitionBuilder builder() {
        return new ItemDefinition.ItemDefinitionBuilder();
    }

    public Long getId() {
        return this.id;
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

    public String getCategory() {
        return this.category;
    }

    public boolean isStackable() {
        return this.stackable;
    }

    public Integer getBuyPrice() {
        return this.buyPrice;
    }

    public Integer getSellPrice() {
        return this.sellPrice;
    }

    public boolean isTradable() {
        return this.tradable;
    }

    public boolean isSellable() {
        return this.sellable;
    }

    public boolean isUsable() {
        return this.usable;
    }

    public Integer getMaxStack() {
        return this.maxStack;
    }

    public String getRarity() {
        return this.rarity;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public void setCategory(final String category) {
        this.category = category;
    }

    public void setStackable(final boolean stackable) {
        this.stackable = stackable;
    }

    public void setBuyPrice(final Integer buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(final Integer sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setTradable(final boolean tradable) {
        this.tradable = tradable;
    }

    public void setSellable(final boolean sellable) {
        this.sellable = sellable;
    }

    public void setUsable(final boolean usable) {
        this.usable = usable;
    }

    public void setMaxStack(final Integer maxStack) {
        this.maxStack = maxStack;
    }

    public void setRarity(final String rarity) {
        this.rarity = rarity;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public ItemDefinition() {
    }

    public ItemDefinition(final Long id, final String code, final String name, final String description, final String category, final boolean stackable, final Integer buyPrice, final Integer sellPrice, final boolean tradable, final boolean sellable, final boolean usable, final Integer maxStack, final String rarity, final String icon) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.stackable = stackable;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.tradable = tradable;
        this.sellable = sellable;
        this.usable = usable;
        this.maxStack = maxStack;
        this.rarity = rarity;
        this.icon = icon;
    }
}
