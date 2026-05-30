package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopProductEntity {

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

    @Column(name = "equipment_template_name")
    private String equipmentTemplateName;

    @Column(nullable = false)
    private int price;

    @Column(name = "sell_price", nullable = false)
    @Builder.Default
    private int sellPrice = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
