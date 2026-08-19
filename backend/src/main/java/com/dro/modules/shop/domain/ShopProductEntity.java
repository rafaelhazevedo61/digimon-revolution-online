package com.dro.modules.shop.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * Componente da camada de componente de domínio do módulo de Loja.
 */
@Entity
@Table(name = "shop_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Transient
    @Builder.Default
    private boolean newEntity = false;

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
