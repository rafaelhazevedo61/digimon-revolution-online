package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.equipment.domain.EquipmentRarity;
import jakarta.persistence.*;

/**
 * Item e quantidade registrados como resultado de uma abertura de baú.
 */
@Entity
@Table(name = "chest_opening_items")
public class ChestOpeningItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chest_opening_id", nullable = false)


    private ChestOpeningEntity chestOpening;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;
    @Column(name = "material_code", length = 80)
    private String materialCode;
    @Column(name = "equipment_template_name", length = 120)
    private String equipmentTemplateName;
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_rarity", length = 20)
    private EquipmentRarity equipmentRarity;
    @Column(nullable = false)
    private int quantity;


    public static class ChestOpeningItemEntityBuilder {
        private Long id;
        private ChestOpeningEntity chestOpening;
        private LootRarity rarity;
        private ItemType itemType;
        private String materialCode;
        private String equipmentTemplateName;
        private EquipmentRarity equipmentRarity;
        private int quantity;

        ChestOpeningItemEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder chestOpening(final ChestOpeningEntity chestOpening) {
            this.chestOpening = chestOpening;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder itemType(final ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder materialCode(final String materialCode) {
            this.materialCode = materialCode;
            return this;
        }

        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder equipmentTemplateName(final String equipmentTemplateName) {
            this.equipmentTemplateName = equipmentTemplateName;
            return this;
        }

        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder equipmentRarity(final EquipmentRarity equipmentRarity) {
            this.equipmentRarity = equipmentRarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningItemEntity.ChestOpeningItemEntityBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        public ChestOpeningItemEntity build() {
            return new ChestOpeningItemEntity(this.id, this.chestOpening, this.rarity, this.itemType, this.materialCode, this.equipmentTemplateName, this.equipmentRarity, this.quantity);
        }

        @Override
        public String toString() {
            return "ChestOpeningItemEntity.ChestOpeningItemEntityBuilder(id=" + this.id + ", chestOpening=" + this.chestOpening + ", rarity=" + this.rarity + ", itemType=" + this.itemType + ", materialCode=" + this.materialCode + ", equipmentTemplateName=" + this.equipmentTemplateName + ", equipmentRarity=" + this.equipmentRarity + ", quantity=" + this.quantity + ")";
        }
    }

    public static ChestOpeningItemEntity.ChestOpeningItemEntityBuilder builder() {
        return new ChestOpeningItemEntity.ChestOpeningItemEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public ChestOpeningEntity getChestOpening() {
        return this.chestOpening;
    }

    public LootRarity getRarity() {
        return this.rarity;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public String getMaterialCode() {
        return this.materialCode;
    }

    public String getEquipmentTemplateName() {
        return this.equipmentTemplateName;
    }

    public EquipmentRarity getEquipmentRarity() {
        return this.equipmentRarity;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setChestOpening(final ChestOpeningEntity chestOpening) {
        this.chestOpening = chestOpening;
    }

    public void setRarity(final LootRarity rarity) {
        this.rarity = rarity;
    }

    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public void setMaterialCode(final String materialCode) {
        this.materialCode = materialCode;
    }

    public void setEquipmentTemplateName(final String equipmentTemplateName) {
        this.equipmentTemplateName = equipmentTemplateName;
    }

    public void setEquipmentRarity(final EquipmentRarity equipmentRarity) {
        this.equipmentRarity = equipmentRarity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public ChestOpeningItemEntity() {
    }

    public ChestOpeningItemEntity(final Long id, final ChestOpeningEntity chestOpening, final LootRarity rarity, final ItemType itemType, final String materialCode, final String equipmentTemplateName, final EquipmentRarity equipmentRarity, final int quantity) {
        this.id = id;
        this.chestOpening = chestOpening;
        this.rarity = rarity;
        this.itemType = itemType;
        this.materialCode = materialCode;
        this.equipmentTemplateName = equipmentTemplateName;
        this.equipmentRarity = equipmentRarity;
        this.quantity = quantity;
    }
}
